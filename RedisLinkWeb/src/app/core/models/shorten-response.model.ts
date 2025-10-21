export interface ShortenResponse {
  successful: boolean;
  message: string;
  shortUrl: string;
  link: LinkDto;
}

export interface LinkDto {
  long_url: string;     
  short: string;        
  hits: number;         
  created_at: string;   
}
