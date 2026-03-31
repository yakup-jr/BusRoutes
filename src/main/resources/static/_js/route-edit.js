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

document.addEventListener('click', function (e) {
    if (!e.target.closest('.relative')) {
        document.querySelectorAll('.search-results').forEach(div => div.classList.add('hidden'));
    }
});

function addStopRow() {
    const container = document.getElementById("stops-container");
    const rows = container.querySelectorAll('.stop-row');
    const index = rows.length;

    const newRow = document.createElement('div');
    newRow.className = 'relative group stop-row bg-white border border-slate-100 rounded-xl p-3 shadow-sm hover:border-[#00bcff]/30 transition-all';

    newRow.innerHTML = `
        <input type="hidden" name="stops[${index}].stopOrder" value="${index + 1}" class="row-order-input" />
        
        <div class="flex items-center gap-3">
            <div class="flex flex-col items-center">
                <span class="text-slate-400 font-mono text-xs row-number">${index + 1}</span>
                <div class="w-0.5 h-4 bg-slate-100 my-1 group-last:hidden"></div>
            </div>

            <div class="relative flex-1">
                <input name="stops[${index}].stopName" 
                       type="text" autocomplete="off" oninput="searchStops(this)"
                       placeholder="Search stop..."
                       class="stop-input w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:bg-white focus:border-[#00bcff] transition-all" />
                <div class="search-results absolute z-30 w-full mt-1 hidden max-h-48 overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-xl"></div>
            </div>

            <div class="flex items-center gap-2 bg-slate-50 rounded-lg px-2 border border-slate-100">
                <svg class="w-3 h-3 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path d="M12 6v6l4 2" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <input name="stops[${index}].arriveAtFromStart" 
                       type="number" value="0" placeholder="Secs"
                       class="w-16 bg-transparent py-2 text-xs font-mono text-slate-600 outline-none" />
            </div>

            <button type="button" onclick="removeStop(this)" 
                    class="p-2 text-slate-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
            </button>
        </div>
    `;

    const addButton = container.nextElementSibling;
    container.appendChild(newRow);

    updateStopCounter();
    container.scrollTo({top: container.scrollHeight, behavior: 'smooth'});
}

function updateStopCounter() {
    const count = document.querySelectorAll('.stop-row').length;
    const badge = document.getElementById('stop-count');
    if (badge) {
        badge.innerText = count + ' STOPS';
    }
}

function removeStop(button) {
    const container = document.getElementById('stops-container');
    const row = button.closest('.stop-row');

    if (container.querySelectorAll('.stop-row').length > 1) {
        row.remove();
        updateRowNumbers();
        updateStopCounter();
    }
}