export const isValidMobile = (number) =>
  /^[6-9]\d{9}$/.test(number);

export const isValidPassword = (password) =>
  password.length >= 8;
