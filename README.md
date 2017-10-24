# Crawler

Holds in memory scrapped pages, allows to add new ones (by URL),
configure max depth of crawling linked page from the root page, max amount of pages that are held in memory,
search on already loaded pages.
When the maximum amount of pages is reached (at the moment when page content is downloaded and parsed) - the latest pages are removed.


#### Not implemented: 
* smart validation on unique URLs ("http://google.com" and "http://www.google.com" and "http://google.com/" and also redirected to index pages will be considered as different)
* correct handling of relative urls
* normal unit tests with mocking of external resources
* integration tests
* search for multiple occurrences of search query
* search and all the validations are very primitive
* there are scenarios when infinite looping is possible


#### To run -
`sbt run`

Run tests - `sbt test`.


#### To try it
The UI part doesn't work, but it is possible to use API by any tool like `curl`.


#### Protocol:
`GET` `http://localhost:9000/pages/count` - amount of links that are stored in memory (both in "Loading" and "Ready" status)

`GET` `http://localhost:9000/pages/urls` - see all stored URLs

`GET` `http://localhost:9000/pages/content` with body `{url: "..."}` - will return the full content of the page

`POST` `http://localhost:9000/pages` with body `{url: "..."}` - add URL into system

`GET` `http://localhost:9000/pages/search` with body `{query: "..."}` - search in 


#### Configuration:
`src/main/resources/application.conf`

`crawler.settings.max-pages-to-store` - maximal size of the stored map
`crawler.settings.max-depth` - maximal depth, if set to `-1` - infinite
