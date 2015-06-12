// Google Map for transit coverage

// Globals
var coordinates;
var map;

var horizontal;
var vertical;
var avg_px;

function initialize() {
  geocoder = new google.maps.Geocoder();
  var mapCanvas = document.getElementById('map-canvas');
  var mapOptions = {
    center: new google.maps.LatLng(47.65743, -122.32315),
    zoom: 8,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  
  map = new google.maps.Map(mapCanvas, mapOptions);
  
  //var transitLayer = new google.maps.TransitLayer();
  //transitLayer.setMap(map);

  var transitLayer = new google.maps.KmlLayer({
    url: 'all-data.kml'
  });

  transitLayer.setMap(map);


  // Event Handlers
  google.maps.event.addListener(map, 'zoom_changed', function() {
    var zoomLevel = map.getZoom()
    document.getElementById("zoom-level").innerHTML=('Zoom: ' + zoomLevel);
  });

  google.maps.event.addListener(map, 'bounds_changed', function() {
    var bounds = map.getBounds();

    var NE = bounds.getNorthEast();
    var SW = bounds.getSouthWest();

    var horizontalLatLng1 = new google.maps.LatLng(NE.lat(), NE.lng());
    var horizontalLatLng2 = new google.maps.LatLng(NE.lat(), SW.lng());
    var verticalLatLng1   = new google.maps.LatLng(NE.lat(), NE.lng());
    var verticalLatLng2   = new google.maps.LatLng(SW.lat(), NE.lng());

    horizontal = google.maps.geometry.spherical.computeDistanceBetween(horizontalLatLng1, horizontalLatLng2);
    vertical   = google.maps.geometry.spherical.computeDistanceBetween(verticalLatLng1,   verticalLatLng2  );

    var horizontal_px = horizontal / 500;
    var vertical_px   = vertical   / 400;
    avg_px = (horizontal_px + vertical_px) / 2;

    document.getElementById("horizontal").innerHTML=('Horz: ' + horizontal);
    document.getElementById("vertical"  ).innerHTML=('Vert: ' + vertical);

    document.getElementById("avg-px").innerHTML=('Meters per pixel: ' + avg_px);

    // Update line width
    //set_transit_line_width(1000 / avg_px);
  });
}

function set_transit_line_width( width ) {
  map.set('styles', [
    {
      featureType: 'transit.line',
      elementType: 'geometry',
      stylers: [
        { color: '#3399FF' },
        { weight: width }
      ]
    }
  ]);
}

// Transit Data


// Getting coordinates
function get_coordinates( callback ) {  
  
  var address = document.getElementById('address').value;
  geocoder.geocode({ address: address}, geocode_return, callback);
  
  function geocode_return( results, status ) {
    if (status == "OK") {
      callback(results[0].geometry.location);
    } else {
      console.log(status)
    }
  }
}

function get_coord_event() {
  get_coordinates(function(coord) {
    window.coordinates = coord;
    console.log(coordinates);
    map.setCenter(coordinates);
  });
}


// Error Handler
function errorHandler(e) {
  console.log('Error: ' + e.code);
}

google.maps.event.addDomListener(window, 'load', initialize);

