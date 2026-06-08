package com.websitewatcher.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
public class ProxyController {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String PICKER_SCRIPT = """
            <script>
            (function() {
              var style = document.createElement('style');
              style.textContent = [
                '* { cursor: crosshair !important; }',
                '.ww-hover { outline: 3px solid #3b82f6 !important; outline-offset: 2px !important; background: rgba(59,130,246,0.08) !important; }',
                '.ww-selected { outline: 3px solid #ef4444 !important; background: rgba(239,68,68,0.08) !important; }',
                '.ww-tooltip { position: fixed; bottom: 12px; left: 50%; transform: translateX(-50%); background: #1e293b; color: #fff; padding: 6px 14px; border-radius: 6px; font: 13px monospace; z-index: 999999; pointer-events: none; max-width: 90vw; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }'
              ].join(' ');
              document.head.appendChild(style);

              var tooltip = document.createElement('div');
              tooltip.className = 'ww-tooltip';
              tooltip.textContent = 'Click an element to select it';
              document.body.appendChild(tooltip);

              var hovered = null;
              var selected = null;

              document.addEventListener('mouseover', function(e) {
                if (hovered && hovered !== selected) hovered.classList.remove('ww-hover');
                hovered = e.target;
                if (hovered !== selected) hovered.classList.add('ww-hover');
                tooltip.textContent = getSelector(hovered);
              }, true);

              document.addEventListener('mouseout', function(e) {
                if (hovered && hovered !== selected) hovered.classList.remove('ww-hover');
              }, true);

              document.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                if (selected) selected.classList.remove('ww-selected');
                selected = e.target;
                selected.classList.remove('ww-hover');
                selected.classList.add('ww-selected');
                var sel = getSelector(selected);
                tooltip.textContent = '✓ Selected: ' + sel;
                window.parent.postMessage({ type: 'ww-selector', selector: sel }, '*');
              }, true);

              function getSelector(el) {
                if (!el || el.nodeType !== 1) return '';
                if (el.id) return '#' + CSS.escape(el.id);
                var path = [];
                var current = el;
                while (current && current.nodeType === 1 && current.tagName.toLowerCase() !== 'body') {
                  var tag = current.tagName.toLowerCase();
                  var seg = tag;
                  if (current.id) { seg = '#' + CSS.escape(current.id); path.unshift(seg); break; }
                  var classes = Array.from(current.classList)
                    .filter(function(c) { return c !== 'ww-hover' && c !== 'ww-selected'; })
                    .slice(0, 2);
                  if (classes.length) seg += '.' + classes.map(function(c){ return CSS.escape(c); }).join('.');
                  var nth = 1;
                  var sib = current.previousElementSibling;
                  while (sib) { if (sib.tagName === current.tagName) nth++; sib = sib.previousElementSibling; }
                  if (nth > 1) seg += ':nth-of-type(' + nth + ')';
                  path.unshift(seg);
                  current = current.parentElement;
                }
                return path.join(' > ');
              }
            })();
            </script>
            """;

    @GetMapping("/internal/proxy")
    public ResponseEntity<String> proxy(@RequestParam String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (compatible; WebsiteWatcher/1.0)")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String html = response.body();

            // strip all script tags so the site's JS doesn't navigate away or cause CORS errors
            html = html.replaceAll("(?is)<script[^>]*>.*?</script>", "");
            // strip noscript tags too
            html = html.replaceAll("(?is)<noscript[^>]*>.*?</noscript>", "");

            // resolve relative URLs so images/CSS load correctly
            String base = "<base href=\"" + url + "\" target=\"_blank\">";
            if (html.toLowerCase().contains("<head>")) {
                html = html.replaceFirst("(?i)<head>", "<head>" + base);
            } else if (html.toLowerCase().contains("<head ")) {
                html = html.replaceFirst("(?i)<head ", "<head" + " data-base-injected=\"true\" " + ">" + base + "<head ");
            } else {
                html = base + html;
            }

            // inject picker script before </body>
            if (html.toLowerCase().contains("</body>")) {
                html = html.replaceFirst("(?i)</body>", PICKER_SCRIPT + "</body>");
            } else {
                html = html + PICKER_SCRIPT;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><p style='font-family:sans-serif;color:#ef4444;padding:2rem'>Failed to load page: " + e.getMessage() + "</p></body></html>");
        }
    }
}
