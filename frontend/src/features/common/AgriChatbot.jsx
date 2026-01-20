import React, { useState, useEffect, useRef, useCallback } from "react";
import { fetchQuestions } from "../../services/chatbotApi";
import "./AgriChatbot.css";

const AgriChatbot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [role, setRole] = useState(null);
  const [lang, setLang] = useState("en");
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState([]);
  const [selectedQuestion, setSelectedQuestion] = useState(null);

  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const loadQuestions = useCallback(async () => {
    if (!role) return;
    setLoading(true);
    try {
      const data = await fetchQuestions(role, lang);
      setQuestions(data);
      setMessages([
        {
          type: "bot",
          text:
            lang === "hi"
              ? "मैं आपकी कैसे मदद कर सकता हूँ?"
              : `How can I help ${role.toLowerCase()}s today?`,
        },
      ]);
    } catch {
      setMessages([
        { type: "bot", text: "Unable to load questions. Please try again." },
      ]);
    } finally {
      setLoading(false);
    }
  }, [role, lang]);

  useEffect(() => {
    if (role) loadQuestions();
  }, [loadQuestions]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, selectedQuestion]);

  const handleQuestionClick = (question, answer) => {
    setSelectedQuestion(question.id);
    setMessages((prev) => [
      ...prev,
      { type: "user", text: question },
      { type: "bot", text: answer },
    ]);
  };

  const handleBack = () => {
    setSelectedQuestion(null);
    setMessages((prev) => [
      ...prev,
      { type: "bot", text: lang === "hi" ? "प्रश्न चुनें" : "Select a question:" },
    ]);
  };

  const resetChat = () => {
    setIsOpen(false);
    setRole(null);
    setQuestions([]);
    setMessages([]);
    setSelectedQuestion(null);
  };

  return (
    <>
      {/* TOGGLE BUTTON – ALWAYS VISIBLE */}
      <div className="chatbot-toggle-wrapper">
        <button
          className="chat-toggle-btn"
          onClick={() => setIsOpen((prev) => !prev)}
          title={isOpen ? "Close chat" : "Open chat"}
        >
          {isOpen ? "🤖" : "💬"}
          <span className="chat-toggle-label">Agri Help</span>
        </button>
      </div>

      {/* CHATBOT WINDOW */}
      {isOpen && (
        <div className="chatbot-wrapper">
          <div className="chat-container">
            {/* HEADER */}
            <div className="chat-header">
              <span>🤖 {role ? role : "Agri"} Support</span>
              <button className="close-btn" onClick={resetChat}>×</button>
            </div>

            {/* MESSAGES */}
            <div className="messages">
              {messages.map((msg, idx) => (
                <div key={idx} className={`message ${msg.type}`}>
                  <span>{msg.text}</span>
                </div>
              ))}

              {loading && (
                <div className="message bot">
                  <span>Loading...</span>
                </div>
              )}

              {/* ROLE SELECTION */}
              {!role && (
                <div className="role-selector-inside">
                  <div className="lang-selector">
                    <span>Language:</span>
                    <button
                      className={lang === "en" ? "active" : ""}
                      onClick={() => setLang("en")}
                    >
                      EN
                    </button>
                    <button
                      className={lang === "hi" ? "active" : ""}
                      onClick={() => setLang("hi")}
                    >
                      हिंदी
                    </button>
                  </div>

                  <button className="farmer-btn" onClick={() => setRole("FARMER")}>
                    👨‍🌾 Farmer
                  </button>
                  <button
                    className="retailer-btn"
                    onClick={() => setRole("RETAILER")}
                  >
                    🏪 Retailer
                  </button>
                </div>
              )}

              {/* QUESTIONS */}
              {!loading && role && !selectedQuestion && (
                <div className="questions-list">
                  {questions.map((q) => (
                    <button
                      key={q.id}
                      className="question-btn"
                      onClick={() =>
                        handleQuestionClick(q.question, q.answer)
                      }
                    >
                      {q.question}
                    </button>
                  ))}
                </div>
              )}

              {/* BACK */}
              {selectedQuestion && (
                <button className="back-btn" onClick={handleBack}>
                  ⬅ Back
                </button>
              )}

              <div ref={messagesEndRef} />
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default AgriChatbot;
