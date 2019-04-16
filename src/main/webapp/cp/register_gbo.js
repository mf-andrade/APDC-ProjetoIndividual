window.onload = function() {
    var frms = document.getElementsByName("register");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        event.preventDefault();
        var frm = document.getElementsByName("register")[0];
        var select = frm.elements["profileStatus"];
        var rd = JSON.stringify({"username": frm.elements["username"].value,
            "password": frm.elements["password"].value,
            "email": frm.elements["email"].value,
            "name": frm.elements["name"].value,
            "profileStatus": select.options[select.selectedIndex].value,
            "phoneNumber": frm.elements["phoneNumber"].value,
            "mobilePhoneNumber": frm.elements["mobilePhoneNumber"].value,
            "address": frm.elements["address"].value
        });
        var obj = {};
        obj.rd = JSON.parse(rd);
        obj.token = JSON.parse(localStorage.getItem("PItoken"));
        console.log(obj);

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/register/gs/gbo",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    //console.log("Logged in " + x.tokenID);
                    alert("Utilizador registado!")
                    window.location.href = "index.html";
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