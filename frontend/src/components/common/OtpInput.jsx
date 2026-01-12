import React, { useRef } from "react";
import "./OtpInput.css";

export default function OtpInput({
  length = 6,
  value = [],
  onChange,
  disabled = false
}) {
  const inputsRef = useRef([]);

  const handleChange = (e, index) => {
    const digit = e.target.value;

    if (!/^\d?$/.test(digit)) return;

    const newValue = [...value];
    newValue[index] = digit;
    onChange(newValue);

    if (digit && index < length - 1) {
      inputsRef.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace" && !value[index] && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasteData = e.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, length);

    if (!pasteData) return;

    const newValue = pasteData.split("");
    while (newValue.length < length) newValue.push("");

    onChange(newValue);

    const nextIndex = Math.min(pasteData.length, length - 1);
    inputsRef.current[nextIndex]?.focus();
  };

  return (
    <div className="otp-container" onPaste={handlePaste}>
      {Array.from({ length }).map((_, index) => (
        <input
          key={index}
          ref={(el) => (inputsRef.current[index] = el)}
          type="text"
          inputMode="numeric"
          maxLength={1}
          value={value[index] || ""}
          disabled={disabled}
          onChange={(e) => handleChange(e, index)}
          onKeyDown={(e) => handleKeyDown(e, index)}
          className="otp-input"
        />
      ))}
    </div>
  );
}
