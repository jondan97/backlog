function showCreateItemTable() {
    document.getElementById("createItemTable").hidden = false;
}

function toggleInputs() {
    const x = document.getElementById("type").value;
    if (x === "BUG" || x === "TASK") {
        document.getElementById("fakeEffort").disabled = false;
        document.getElementById("fakeStoryParentId").hidden = false;
        document.getElementById("fakeStoryParentId").disabled = false;
        document.getElementById("fakeEpicParentId").hidden = true;
        document.getElementById("disabledParentInput").remove();
    } else if (x === "STORY") {
        document.getElementById("fakeEffort").disabled = true;
        document.getElementById("fakeStoryParentId").hidden = true;
        document.getElementById("fakeEpicParentId").hidden = false;
        document.getElementById("fakeEpicParentId").disabled = false;
        document.getElementById("disabledParentInput").remove();
    } else if (x === "EPIC") {
        document.getElementById("fakeEffort").disabled = true;
        const x = document.getElementById("fakeStoryParentId");
        const option = document.createElement("option");
        option.text = "Disabled";
        option.id = "disabledParentInput";
        option.setAttribute('selected', 'selected');
        x.add(option);
        document.getElementById("fakeStoryParentId").disabled = true;
        document.getElementById("fakeEpicParentId").disabled = true;
        document.getElementById("fakeStoryParentId").hidden = false;
        document.getElementById("fakeEpicParentId").hidden = true;
    }
}

function checkInputs() {
    const x = document.getElementById("type").value;
    if (x === "BUG" || x === "TASK") {
        document.getElementById("effort").value = document.getElementById("fakeEffort").value;
        document.getElementById("parentId").value = document.getElementById("fakeStoryParentId").value;
    } else if (x === "STORY") {
        document.getElementById("effort").value = document.getElementById("fakeEffort").value;
        document.getElementById("parentId").value = document.getElementById("fakeEpicParentId").value;
    }
}

function openViewItemModal(projectId, itemId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}

function updateAssignee(itemId) {
    $('#updateAssignee' + itemId).click();
}

function redirectToModal(projectId, itemId) {
    $('.modal-body').load('/user/project/' + projectId + '/item/' + itemId + '/?source=projectPage', function () {
        $('#viewItemModal').modal({show: true});
    });
}