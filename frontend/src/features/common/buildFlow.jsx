// buildFlow.js
export const buildFlow = (questions, lang = "en") => {
  const labels = {
    en: { 
      select: "Select a question", 
      back: "⬅ Back to questions",
      helpFarmer: "How can I help farmers today?",
      helpRetailer: "How can I help retailers today?"
    },
    hi: { 
      select: "प्रश्न चुनें", 
      back: "⬅ प्रश्न सूची पर वापस जाएं",
      helpFarmer: "किसानों की आज कैसे मदद कर सकता हूँ?",
      helpRetailer: "व्यापारियों की आज कैसे मदद कर सकता हूँ?"
    }
  };
  
  const text = labels[lang] || labels.en;

  // Base flow structure
  const flow = {
    // 1. Role selection (fallback if no role selected)
    start: {
      message: lang === "hi" ? "अपनी भूमिका चुनें" : "Select your role",
      options: [
        { label: "👨‍🌾 Farmer", value: "FARMER" },
        { label: "🏪 Retailer", value: "RETAILER" }
      ]
    },

    // 2. Farmer role flow
    FARMER: {
      message: text.helpFarmer,
      options: [
        { label: text.select, value: "questions_start" }
      ]
    },

    // 3. Retailer role flow  
    RETAILER: {
      message: text.helpRetailer,
      options: [
        { label: text.select, value: "questions_start" }
      ]
    },

    // 4. Questions list (from your backend)
    questions_start: {
      message: questions.length ? text.select : "No questions available right now.",
      options: questions.length ? questions.map(q => ({
        label: String(q.question).substring(0, 50) + (String(q.question).length > 50 ? '...' : ''),
        value: `q_${q.id}`
      })) : []
    }
  };

  // 5. Individual question answers
  questions.forEach(q => {
    flow[`q_${q.id}`] = {
      message: String(q.answer),
      options: [
        { label: text.back, value: "questions_start" }
      ]
    };
  });

  return flow;
};
