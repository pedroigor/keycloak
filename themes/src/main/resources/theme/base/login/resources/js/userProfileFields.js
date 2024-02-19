// @ts-check
/**
 * @typedef {Object} FieldDescriptor
 * @property {string} name - The name of the field to register (e.g. `numberFormat`).
 * @property {(element: HTMLInputElement) => (() => void) | void} onMount - The function to call when a new element is added to the DOM.
 */

const observer = new MutationObserver(onMutate);
observer.observe(document.body, { childList: true, subtree: true });

/** @type {FieldDescriptor[]} */
const registeredFields = [];

/** @type {WeakMap<HTMLInputElement, () => void>} */
const cleanupFunctions = new WeakMap();

/**
 * @param {FieldDescriptor} descriptor
 */
export function registerField(descriptor) {
  registeredFields.push(descriptor);

  document.querySelectorAll(`[data-${descriptor.name}]`).forEach((element) => {
    if (element instanceof HTMLInputElement) {
      mountFieldOnNode(element, descriptor);
    }
  });
}

/**
 * @type {MutationCallback}
 */
function onMutate(mutations) {
  const removedNodes = mutations.flatMap((mutation) => Array.from(mutation.removedNodes));

  for (const node of removedNodes) {
    if (!(node instanceof HTMLInputElement)) {
      continue;
    }

    const cleanup = cleanupFunctions.get(node);

    if (cleanup) {
      cleanup();
    }
  }

  const addedNodes = mutations.flatMap((mutation) => Array.from(mutation.addedNodes));

  for (const descriptor of registeredFields) {
    for (const node of addedNodes) {
      if (node instanceof HTMLInputElement && node.hasAttribute(`data-${descriptor.name}`)) {
        mountFieldOnNode(node, descriptor);
      }
    }
  }
}

/**
 * @param {HTMLInputElement} element
 * @param {FieldDescriptor} descriptor
 */
function mountFieldOnNode(element, descriptor) {
  const cleanup = descriptor.onMount(element);

  if (cleanup) {
    cleanupFunctions.set(element, cleanup);
  }
}
