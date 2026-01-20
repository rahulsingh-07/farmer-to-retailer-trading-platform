export async function fetchQuestions(role, lang = "en") {

  const response = await fetch(
    `http://localhost:8081/auth/chatbot/questions?role=${encodeURIComponent(role)}&lang=${lang}`,
    {
      headers: {
        "Content-Type": "application/json"
      }
    }
  );

  if (!response.ok) {
    throw new Error(`HTTP error! Status: ${response.status}`);
  }

  const apiResponse = await response.json();

  if (!apiResponse.success) {
    throw new Error(apiResponse.message || "Failed to fetch chatbot questions");
  }

  return apiResponse.data;
}
