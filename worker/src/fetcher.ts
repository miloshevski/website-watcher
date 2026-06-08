import axios from 'axios';
import * as cheerio from 'cheerio';

export async function fetchContent(url: string, selector: string | null): Promise<string> {
  const response = await axios.get<string>(url, { timeout: 10000 });
  const $ = cheerio.load(response.data);

  if (selector) {
    return $(selector).text().trim();
  }

  return $('body').text().trim();
}
