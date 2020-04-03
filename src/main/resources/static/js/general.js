function setCookie(cname, cvalue) {
    document.cookie = cname + "=" + cvalue + ";" + "path=/";
}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function checkCookie(cname) {
    let username = getCookie(cname);
    if (username !== "") {
        return true;
    } else {
        return false;
    }
}

function toggleContainerBottom() {
    if (checkCookie("hints")) {
        document.getElementById("hintsON").style.display = "none";
    } else {
        document.getElementById("hintsOFF").style.display = "none";
    }
}

function openHintModal(id) {
    let newHintTitle = $("#hintTitle-" + id).clone();
    let newHintBody = $("#hintBody-" + id).clone();
    newHintTitle.show();
    newHintBody.show();
    $("#hintModal").modal({show: true});
    $(newHintTitle).clone().appendTo("#hintTitle");
    $(newHintBody).clone().appendTo("#hintBody");
    // $("#hintTitle").replaceWith((newHintTitle));
    // $("#hintBody").replaceWith((newHintBody));
}

function showHints() {
    let cookieExists = checkCookie("hints");
    if (cookieExists) {
        $(".hintSymbol").removeAttr('hidden');
    }
}

//delete content on close
$(".modal").on("hidden.bs.modal", function () {
    $("#hintTitle").html("");
    $("#hintBody").html("");
});