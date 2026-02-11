async function fetchBuilds() {
    const listElement = document.getElementById('build-list');
    
    try {
        const response = await fetch('/api/v1/builds');
        const data = await response.json();

        const builds = data.builds; //from BuildListResponse DTO

        listElement.innerHTML = ''; //in case we do some auto/live update 

        builds.forEach(build => {
            const row = document.createElement('tr');
            const uniqueUrl = `build.html?id=${build.buildId}`;

            row.innerHTML = `
                <td>#${build.buildId}</td>
                <td><code>${build.commitSha}</code></td>
                <td>${build.createdAt}</td>
                <td><a href="${uniqueUrl}" class="view-btn">View Logs</a></td>`;
            listElement.appendChild(row);
        });
        } catch (error) {
            listElement.innerHTML = '<tr><td colspan="6">Failed to load builds</td></tr>';
            console.error("Error:", error);
    }
}

fetchBuilds();