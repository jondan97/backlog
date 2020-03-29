function toggleHints() {
    let cookieExists = checkCookie("hints");
    if (cookieExists) {
        setCookie("hints", "");
        document.getElementById("hintsON").style.display = "inline-block";
        document.getElementById("hintsOFF").style.display = "none";
    } else {
        setCookie("hints", "true");
        document.getElementById("hintsON").style.display = "none";
        document.getElementById("hintsOFF").style.display = "inline-block";
    }
}