async function searchStops(input) {
    const query = input.value;
    const resultsDiv = input.parentElement.querySelector('.search-results');

    if (query.length < 2) {
        resultsDiv.classList.add('hidden');
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/api/v1/stop/search?part=${encodeURIComponent(query)}&size=5`);
        const data = await response.json();
        const stops = data.content;

        if (stops && stops.length > 0) {
            resultsDiv.innerHTML = stops.map(stop => `
                <div class="px-4 py-2 hover:bg-blue-50 cursor-pointer text-sm text-slate-700 border-b border-slate-50 last:border-none"
                     onclick="selectStop(this, '${stop.name.replace(/'/g, "\\'")}')">
                    ${stop.name}
                </div>
            `).join('');
            resultsDiv.classList.remove('hidden');
        } else {
            resultsDiv.classList.add('hidden');
        }
    } catch (error) {
        console.error('Search failed:', error);
    }
}

function selectStop(element, stopName) {
    const wrapper = element.closest('.relative');
    const input = wrapper.querySelector('.stop-input');
    const resultsDiv = wrapper.querySelector('.search-results');

    input.value = stopName;
    resultsDiv.classList.add('hidden');
    resultsDiv.innerHTML = '';
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('.relative')) {
        document.querySelectorAll('.search-results').forEach(div => div.classList.add('hidden'));
    }
});

function addStopRow() {
    const container = document.getElementById("stops-container");
    const rows = container.querySelectorAll('.stop-row');
    const index = rows.length;

    const newRow = document.createElement('div');
    newRow.className = 'relative group stop-row border-b border-slate-100 pb-3 last:border-0';

    newRow.innerHTML = `
        <input type="hidden" name="stops[${index}].stopOrder" value="${index + 1}" class="row-order-input" />
        <div class="flex flex-col gap-2">
            <div class="flex items-center gap-2">
                <span class="text-slate-400 font-mono text-xs row-number">${index + 1}.</span>
                
                <div class="relative flex-1"> 
                    <input name="stops[${index}].stopName" type="text" autocomplete="off" 
                           oninput="searchStops(this)" 
                           placeholder="Stop Name"
                           class="stop-input w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-[#00bcff] focus:ring-2 focus:ring-[#00bcff]/20" />
                    
                    <div class="search-results absolute [email protected] w-full mt-1 hidden max-h-48 overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-xl"></div>
                </div>
        
                <div class="w-24">
                    <input name="stops[${index}].arriveAtFromStart" type="number" value="0" 
                           class="w-full rounded-lg border border-slate-200 bg-slate-50 px-2 py-2 text-sm font-mono text-slate-600 outline-none focus:border-[#00bcff]"/>
                </div>

                <button type="button" onclick="removeStop(this)"
                        class="text-slate-300 hover:text-red-500 transition-colors">
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                </button>
            </div>
        </div>
    `;

    const addButton = container.querySelector('button[onclick="addStopRow()"]');
    container.insertBefore(newRow, addButton);
}

function removeStop(button) {
    const container = document.getElementById('stops-container');
    const row = button.closest('.stop-row');

    if (container.querySelectorAll('.stop-row').length > 1) {
        row.remove();
        updateRowNumbers();
    }
}

function updateRowNumbers() {
    const container = document.getElementById('stops-container');
    const rows = container.querySelectorAll('.stop-row');

    rows.forEach((row, i) => {
        row.querySelector('.row-number').innerText = (i + 1) + '.';
        row.querySelector('.row-order-input').value = i + 1;

        const inputs = row.querySelectorAll('input');
        inputs.forEach(input => {
            const currentName = input.getAttribute('name');
            if (currentName) {
                const newName = currentName.replace(/stops\[\d+\]/, `stops[${i}]`);
                input.setAttribute('name', newName);
            }
        });
    });
}