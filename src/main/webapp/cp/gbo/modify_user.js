window.onload = function() {
    var frms = document.getElementsByName("modify");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        event.preventDefault();
        var frm = document.getElementsByName("modify")[0];
        var select = frm.elements["profileStatus"];
        var mud = JSON.stringify({
            "password": frm.elements["password"].value,
            "email": frm.elements["email"].value,
            "profileStatus": select.options[select.selectedIndex].value,
            "phoneNumber": frm.elements["phoneNumber"].value,
            "mobilePhoneNumber": frm.elements["mobilePhoneNumber"].value,
            "address": frm.elements["address"].value,
            "token": null
        });
        var obj = {};
        obj.mud = JSON.parse(mud);
        obj.token = JSON.parse(localStorage.getItem("PItoken"));
        obj.username = frm.elements["username"].value;
        console.log(obj);

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/modify/gbo",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    //console.log("Logged in " + x.tokenID);
                    alert("Utilizador alterado!");
                    window.location.href = "../index.html";
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