# EmailScraper
To run EmailScraper1.java, copy that file and the jsoup.jar file to a directory.  Cd to that directory, compile,
then run with one argument for the domain name.

e.g., for Windows 7:

compile the file: javac -cp Jsoup-1.8.3.jar EmailScraper1.java

run the file with one argument for the domain name: java -cp Jsoup-1.8.3.jar;. EmailScraper1 "domain_name.com"

It can take a long time to crawl through a complete site (on most sites, hundreds of pages are searched). 
The program prints out the emails and corresponding pages as it finds
them, and when it finishes, it prints out a message and lists the emails again.  


EmailScraper1 is the original version.
First I used a recursive, depth-first strategy to go through the pages of a site, but when I saw that I got 
in to hundreds of levels of recursion because on some sites each page had so many links, I switched to a breadth first 
iterative strategy in which I used a queue to store the new links scraped from each page visited.

One problem I ran into when I tried it on the jana.com site was certificate issues for https sites.
I lifted a solution (the enableSSLSocket() function) from here: 
http://nanashi07.blogspot.ru/2014/06/enable-ssl-connection-for-jsoup.html

This much was pretty easy and done in a reasonable amount of time, but the results weren't great.  It still seemed slow to me,
though on most sites, I was crawling through hundreds of pages, so I suppose it was to be expected that it could take several
minutes (or more--though I just bailed after five minutes or so) to get through a complete site.  

Then I put in some more time trying to solve some of the following problems in EmailScraper2:

1) visiting unecessary pages:  
  A lot of media pages should be bypassed.  I tried to address this by first getting the Jsoup content type and screening for
  the types that could be turned in to a Jsoup Document object, (thus screening out most media types, e.g. .jpg, .wmv, .pdf, etc.
  
  I also screened out calendar pages (one site had thousands of old calendar pages) and pages with "?" (like php?), and messed
  messed around with my regular expressions for http links.
  
2) not visiting necessary links:
  Some sites have email addresses buried in scripts instead of as part of the html code.  So at each page I added links to scripts
  as well as links to http links and searched those for emails.  I did not look for additional page links in scripts.
  I did come accross an interesting possibility of using a gui-less browser like htmlUnit to load the page and then scrape
  the page after the script loads.
  
3) Some sites (e.g. jana.com) have links that appear as arguments to a changeRoute() function. 
e.g. <li><span ng-click="changeRoute('contact')">Contact</span></li>
This was the only reference I saw to the contact page, i.e. nowhere did I come accross a link to "jana.com/contact" as
I did for pages like "jana.com/about".
I couldn't think of a way to get these links without searching for the "changeRoute(" string and that seemed too ad hoc.


4) It's slow loading so many pages to search them.  Some kind of parallel processing would help here.  The returns really seemed 
diminishing for my efforts at filtering pages.  (Dying to know some better solutions!)



