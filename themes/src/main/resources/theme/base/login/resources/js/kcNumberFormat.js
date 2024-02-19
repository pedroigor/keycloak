// @ts-check
import { formatNumber } from "./common.js";
import { registerField } from "./userProfileFields.js";

const KC_NUMBER_FORMAT = "kcNumberFormat";

registerField({
  name: KC_NUMBER_FORMAT,
  onMount(input) {
    const formatValue = () => {
      const format = input.getAttribute(`data-${KC_NUMBER_FORMAT}`);
      input.value = formatNumber(input.value, format);
    };

    input.addEventListener("keyup", formatValue);

    return () => input.removeEventListener("keyup", formatValue);
  },
});
