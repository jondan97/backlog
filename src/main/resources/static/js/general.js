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
}

//delete content on close
$(".modal").on("hidden.bs.modal", function () {
    $("#hintTitle").html("");
    $("#hintBody").html("");
});

function showHints() {
    let cookieExists = checkCookie("hints");
    if (cookieExists) {
        $(".hintSymbol").removeAttr('hidden');
    }
}

/* Function for the notification message */
function showNotificationMessage(text, color) {
    var topVar = 0;
    var increment = 1 / 10;
    $('#notificationMessageCloseButtonSymbol').attr('hidden', false);
    var notifierBox = $('.notifierBox');
    notifierBox.css('background-color', color);
    /* animation function */
    $('.notifier').html(text);
    notifierBox.fadeIn();
    if (!id)
        var id = setInterval(frame, 50);

    function frame() {
        if (topVar === 0.8 || topVar > 0.8) {
            clearInterval(id);
            setTimeout(function () {
                notifierBox.fadeOut(250);
            }, 2000)
        } else {
            topVar += increment;
            notifierBox.css('top', (topVar + 'vh'));
        }
    }
}

