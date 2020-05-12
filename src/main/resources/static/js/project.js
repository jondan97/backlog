function showCreateItemTable() {
    let createItemTableHiddenStatus = document.getElementById("createItemTable").hidden;
    if (createItemTableHiddenStatus === true) {
        document.getElementById("createItemTable").hidden = false;
    } else {
        document.getElementById("createItemTable").hidden = true;
    }
}

function toggleInputs() {
    const x = document.getElementById("type").value;
    if (x === "STORY") {
        document.getElementById("fakeEpicParentId").hidden = false;
        document.getElementById("fakeEpicParentId").disabled = false;
        //check if it exists, if yes remove
        const epicParentDisabledInput = document.getElementById("disabledParentInput");
        if (epicParentDisabledInput) {
            epicParentDisabledInput.remove();
        }
    } else if (x === "EPIC") {
        const x = document.getElementById("fakeEpicParentId");
        const option = document.createElement("option");
        option.text = "Disabled";
        option.id = "disabledParentInput";
        option.setAttribute('selected', 'selected');
        x.add(option);
        document.getElementById("fakeEpicParentId").disabled = true;
        document.getElementById("fakeEpicParentId").hidden = false;
    }
}

function checkInputs() {
    const x = document.getElementById("type").value;
    if (x === "STORY") {
        document.getElementById("parentId").value = document.getElementById("fakeEpicParentId").value;
    }
}

function openViewItemModal(projectId, itemId) {
    $('#viewItemModalBody').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}

function updateAssignee(itemId) {
    $('#updateAssignee' + itemId).click();
}

function redirectToModal(projectId, itemId) {
    $('#viewItemModalBody').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}

function createItemOnTheGo(modal, backlog, parentId) {
    let typeString;
    if (modal === 1) {
        $('#createSprintTaskFormModal').modal({show: true});
        typeString = "SprintTask";
    } else if (modal === 2) {
        $('#createStoryFormModal').modal({show: true});
        typeString = "StoryItem";
    }

    let itemForm;
    let alternativeItemForm;
    if (backlog === 1) {
        itemForm = $("#create" + typeString + "ForProject");
        alternativeItemForm = $("#create" + typeString + "ForSprint");
    }
    if (backlog === 0 || backlog === 2 || backlog === 3) {
        itemForm = $("#create" + typeString + "ForSprint");
        alternativeItemForm = $("#create" + typeString + "ForProject");
    }
    $("#create" + typeString + "ParentStatus").val(backlog);
    alternativeItemForm.attr('hidden', true);
    itemForm.attr('hidden', false);
    itemForm.find("select").val(parentId);
    itemForm.find("select").attr('name', 'parentId');
    alternativeItemForm.find("select").attr('name', 'fakeParentId');
}

function showSprintFinishedModal() {
    $('#sprintHasFinishedModal').modal({show: true});
}

function showZeroEffortModal() {
    $('#zeroEffortModal').modal({show: true});
}

