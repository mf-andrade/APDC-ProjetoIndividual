window.onload = function() {
    var token = localStorage.getItem("PItoken");
    if (token === null) {
        alert("Não tem sessão iniciada");
        window.location.href = "../index.html";
    } else
        console.log(token);
}

function logout() {
    $.ajax({
        type: "POST",
        url: "https://axial-analyzer-233813.appspot.com/rest/logout",
        contentType: "application/json",
        crossDomain: true,
        success: function(response) {
            if (response) {
                localStorage.removeItem("PItoken");
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
        data: localStorage.getItem("PItoken")
    });
}

function deleteAccount() {
    var msgbox = confirm("Quer mesmo apagar a conta?");
    if (msgbox == true) {
        $.ajax({
            type: "POST",
            url: "https://axial-analyzer-233813.appspot.com/rest/remove",
            contentType: "application/json",
            crossDomain: true,
            success: function(response) {
                if (response) {
                    localStorage.removeItem("PItoken");
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
            data: localStorage.getItem("PItoken")
        });
    }
}