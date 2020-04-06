function showCreateProjectTable() {
    document.getElementById("createProjectTable").hidden = false;
}

// When the user clicks on the button, open the modal
function modifyProject(projectCount) {
    $('#editProjectModal').modal({show: true});
    //modal.style.display = "block";
    document.getElementById("projectId").value = document.getElementById("projectId" + projectCount).value;
    document.getElementById("projectTitle").value = document.getElementById("projectTitle" + projectCount).value;
    document.getElementById("projectDescription").value = document.getElementById("projectDescription" + projectCount).value;
    document.getElementById("projectDevelopersWorking").value = document.getElementById("projectDevelopersWorking" + projectCount).value;
    let sprintDurationValue = document.getElementById("projectSprintDuration" + projectCount).value;
    if (sprintDurationValue == 1) {
        document.getElementById("projectSprintDurationOption1").selected = true;
    }
    if (sprintDurationValue == 2) {
        document.getElementById("projectSprintDurationOption2").selected = true;
    }
    if (sprintDurationValue == 3) {
        document.getElementById("projectSprintDurationOption3").selected = true;
    }
    if (sprintDurationValue == 4) {
        document.getElementById("projectSprintDurationOption4").selected = true;
    }
}


