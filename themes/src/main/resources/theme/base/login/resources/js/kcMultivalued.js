const DATA_KC_MULTIVALUED = 'data-kcMultivalued';

const KC_ADD_ACTION_PREFIX = "kc-add-";
const KC_REMOVE_ACTION_PREFIX = "kc-remove-";

function createAddAction(element) {
    const action = document.createElement("a")
    action.setAttribute("id", KC_ADD_ACTION_PREFIX + element.getAttribute("id"));
    action.setAttribute("href", "#");
    action.innerText = "Add value";
    action.addEventListener("click", ev => {
        const name = element.getAttribute("name");
        const elements = getInputElementsByName().get(name);
        const length = elements.length;

        if (length === 0) {
            return;
        }

        const lastNode = elements[length - 1];
        const newNode = lastNode.cloneNode(true);
        newNode.setAttribute("id", name + "-" + elements.length);
        newNode.value = "";
        lastNode.after(newNode);
    });

    element.after(action);
}

function createRemoveAction(element, isLastElement) {
    const action = document.createElement("a")
    action.setAttribute("id", KC_REMOVE_ACTION_PREFIX + element.getAttribute("id"));
    action.setAttribute("href", "#");
    action.innerText = "Remove";

    if (isLastElement) {
        action.innerHTML = action.innerText + " | ";
    }

    action.addEventListener("click", ev => {
        removeActions(element);
        element.remove();
        renderAttributes();
    });

    element.insertAdjacentElement('afterend', action);
}

function getInputElementsByName() {
    const selector = document.querySelectorAll(`[${DATA_KC_MULTIVALUED}]`);
    const elementsByName = new Map();

    for (let element of Array.from(selector.values())) {
        let name = element.getAttribute("name");
        let elements = elementsByName.get(name);

        if (!elements) {
            elements = [];
            elementsByName.set(name, elements);
        }

        elements.push(element);
    }

    return elementsByName;
}

function removeActions(element) {
    for (let actionPrefix of [KC_ADD_ACTION_PREFIX, KC_REMOVE_ACTION_PREFIX]) {
        const action = document.getElementById(actionPrefix + element.getAttribute("id"));

        if (action) {
            action.remove();
        }
    }
}

export function render() {
    getInputElementsByName().forEach((elements, name) => {
        elements.forEach((element, index) => {
            removeActions(element);

            element.setAttribute("id", name + "-" + index);

            const lastNode = element === elements[elements.length - 1];

            if (lastNode) {
                createAddAction(element);
            }

            if (elements.length > 1) {
                createRemoveAction(element, lastNode);
            }
        });
    });
}

render();