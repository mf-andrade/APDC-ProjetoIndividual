window.onload = function() {
    var frms = document.getElementsByName("userdata");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        event.preventDefault();
        var frm = document.getElementsByName("userdata")[0];
        var obj = {};
        obj.token = JSON.parse(localStorage.getItem("PItoken"));
        obj.username = frm.elements["username"].value;
        console.log(obj);

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/gbo/getuserdata",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    console.log(response);
                    document.getElementById("email").innerHTML = response.email;
                    document.getElementById("name").innerHTML = response.name;
                    document.getElementById("profile").innerHTML = response.profileStatus;
                    document.getElementById("phone").innerHTML = response.phoneNumber;
                    document.getElementById("mphone").innerHTML = response.mobilePhoneNumber;
                    document.getElementById("address").innerHTML = response.address;
                } else {
                    alert("No response");
                }
            },
            error: function(response) {
                var x = JSON.stringify(response);
                var y = JSON.parse(x);
                //console.log(y);
                alert("Error: " + y.responseText);
            },
            data: JSON.stringify(obj)
        });
    };
}