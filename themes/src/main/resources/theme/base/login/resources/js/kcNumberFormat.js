import {formatNumber} from "./common.js";
import {registerField} from "./userProfileFields.js"

const KC_NUMBER_FORMAT = 'data-kcNumberFormat';

function enhanceInput(input) {
    const format = input.getAttribute(KC_NUMBER_FORMAT);

    input.addEventListener('keyup', (event) => {
        input.value = formatNumber(input.value, format);
    });

    input.value = formatNumber(input.value, format);
}

registerField(KC_NUMBER_FORMAT, enhanceInput);