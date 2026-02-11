async function loadBuildDetails(){
    const urlParams = new URLSearchParams(window.location.search);
    const buildId = urlParams.get('id');

    if (!buildId) {
            console.error("No buildID found");
            return;
        }

    try{
        const metaResponse = await fetch(`/api/v1/builds/${buildId}`);
        const metaData = await metaResponse.json();

        const logResponse = await fetch(`/api/v1/builds/${buildId}/log`);
        const logData = await logResponse.json();

        displayData(metaData, logData);
    } 
    catch(error){
        console.error("Communication error", error);
    }

    function displayData(meta, logBody){
        document.getElementById('header-id').innerText = `Build #${meta.buildId}`;
        document.getElementById('build-status').innerText = meta.status;
        document.getElementById('build-commit').innerText = meta.commitSha;
        document.getElementById('build-owner').innerText = meta.owner;
        document.getElementById('build-date').innerText = meta.createdAt;
        document.getElementById('log-output').innerText = logBody.log;
    }
}

loadBuildDetails()