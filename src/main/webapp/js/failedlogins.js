window.onload = function() {
    var frms = document.getElementsByName("failed");
    console.log(frms[0]);
    frms[0].onsubmit = function(event) {
        var frm = document.getElementsByName("failed")[0];
        var x = JSON.stringify({"username": frm.elements["username"].value, "failedAttempts": 0});
        console.log(JSON.parse(x));

        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/gs/getfailedattempts",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    alert("Número de tentativas de login falhadas: " + response.failedAttempts);
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