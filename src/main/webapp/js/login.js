window.onload = function() {
    var frms = document.getElementsByName("login");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        var frm = document.getElementsByName("login")[0];
        var x = JSON.stringify({"username": frm.elements["username"].value,
            "password": frm.elements["password"].value
        });
        console.log(JSON.parse(x));

        /*var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
					alert(this.responseText);
            }
		};
		xhttp.open("POST", "https://axial-analyzer-233813.appspot.com/rest/login", true);
        xhttp.setRequestHeader("Content-Type", "application/json");
        xhttp.send(x);
        */

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/login",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    var x = JSON.parse(JSON.stringify(response));
                    console.log("Logged in " + x.tokenID);
                    localStorage.setItem("PItoken", JSON.stringify(response));
                    window.location.href = "cp/index.html";
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
            data: x
        });

        event.preventDefault();
    };
}