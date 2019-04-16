window.onload = function() {
    var frms = document.getElementsByName("remove");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        event.preventDefault();
        var frm = document.getElementsByName("remove")[0];
        var obj = {};
        obj.token = JSON.parse(localStorage.getItem("PItoken"));
        obj.username = frm.elements["username"].value;
        console.log(obj);

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/remove/gbo",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    //console.log("Logged in " + x.tokenID);
                    alert("Utilizador removido!");
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