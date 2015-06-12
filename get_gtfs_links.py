from selenium import webdriver
from pyvirtualdisplay import Display

url = "http://www.gtfs-data-exchange.com/agencies"

d = webdriver.Firefox()

d.get(url)

link_elements = d.find_elements_by_css_selector("ol.agency_list li a")

links = []
for element in link_elements:
    links.append(element.get_attribute('href'))


f = open("links.txt", 'w')


for link in links:
    d.get(link)

    link_elements = d.find_elements_by_css_selector("a")
    agency = link.split('/')[-2]

    for element in link_elements:
        url = element.get_attribute('href')
        if url.endswith(".zip"):
            f.write(agency + " " + element.get_attribute('href') + "\n")
            break


f.close()
