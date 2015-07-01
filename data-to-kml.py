import xml.etree.ElementTree as ET
import json

# Vars
NAME = "Transit Shapes"
DESC = "Shapes of Transit Routes"
COLOR_LINE = "ffff0000"
COLOR_POLY = "ff0000ff"
WIDTH = "4"

FILENAME = "all-data-export.txt"
OUTPUT   = 'all-data.kml'


HEADER = '<?xml version="1.0" encoding="UTF-8"?>'
KML_ATTRB = {
    "xmlns"     : "http://www.opengis.net/kml/2.2",
    "xmlns:gx"  : "http://www.google.com/kml/ext/2.2",
    "xmlns:kml" : "http://www.opengis.net/kml/2.2",
    "xmlns:atom": "http://www.w3.org/2005/Atom"
}


# Funcs

def appendNew(tag, text, parent):
    element = ET.Element(tag)
    element.text = text
    parent.append(element)


def createStyle(line_color, poly_color, width, url):
    style = ET.Element('Style', attrib={'id' : url} )

    line_style = ET.Element('LineStyle')
    
    appendNew('color', line_color, line_style)
    appendNew('width', width, line_style)
    
    poly_style = ET.Element('PolyStyle')
    appendNew('color', poly_color, poly_style)
    
    style.append(line_style)
    style.append(poly_style)
    
    return style

def createPlacemark(name, style_url, coords):
    placemark = ET.Element('Placemark')
    appendNew('name', name, placemark)
    appendNew('styleUrl', style_url, placemark)

    line_string = ET.Element('LineString')
    #appendNew('extrude', '1', line_string)
    #appendNew('tessellate', '1', line_string)
    appendNew('altitudeMode', 'relative', line_string)
    appendNew('coordinates', coords, line_string)

    placemark.append(line_string)

    return placemark



# Main Code

colors = ["#FF0000", "#0000FF", "#800080", "#FFFF00", "#FFA500", "#008000", "#00FFFF", "#00FF00", "#000000", "#A52A2A", "#808000", "#FF00FF", "#FFFFFF", "#0000A0", "#ADD8E6"]
c_text = ["red", "blue", "purple", "yellow", "orange", "green", "cyan", "lime", "black", "brown", "olive", "magenta", "white", "dark blue", "light blue"]

root = ET.Element('kml', attrib=KML_ATTRB)
document = ET.Element('Document')
root.append(document)

appendNew('name', NAME, document)
appendNew('description', DESC, document)

styles = []
i = 0

f = open(FILENAME, 'r').read()
data = json.loads(f)

for agency in data:
    color = colors[i]
    
    print("Processing agency " + agency + "(color: " + c_text[i] + ")")
    styles.append(createStyle(color, color, WIDTH, "#" + agency))

    i += 1

    for route in data[agency]:
        coords  = ""
        for point in data[agency][route]:
            coords += point[1] + "," + point[0] + " "

        placemark = createPlacemark(agency + "." + route, "#" + agency, coords)
        document.append(placemark)


for style in styles:
    document.append(style)

    
et = ET.ElementTree(root)
et.write(OUTPUT)



            

