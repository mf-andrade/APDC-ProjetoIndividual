var map;

function initMap() 
{   
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4) {
            if (this.status == 200) {
                var p = JSON.parse(this.responseText);
                console.log(p);
                map = new google.maps.Map(document.getElementById('map'), 
                {
                    center: {lat: p.lat, lng: p.lon},
                    zoom: 16
                });
            } else
            alert(this);
        }
    };
    xhttp.open("POST", "https://axial-analyzer-233813.appspot.com/rest/user/getmap", true);
    xhttp.setRequestHeader("Content-Type", "application/json");
    xhttp.send(localStorage.getItem("PItoken"));
}