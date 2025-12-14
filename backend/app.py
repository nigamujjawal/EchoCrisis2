from flask import Flask, request, jsonify
import google.generativeai as genai
import os

app = Flask(__name__)

# -------------------------------------------------------------------------
# 🔴 IMPORTANT: REPLACE 'YOUR_API_KEY_HERE' WITH YOUR ACTUAL GEMINI API KEY
# You can get one for free at: https://aistudio.google.com/app/apikey
# -------------------------------------------------------------------------
GEMINI_API_KEY = "AIzaSyAj3US_xXyZ7NFoAQtse_dWTRZzePAGYws"

# Configure Gemini
try:
    if GEMINI_API_KEY == "YOUR_API_KEY_HERE":
        print("⚠️  WARNING: You haven't set your Gemini API Key in app.py yet! The bot will fail until you do.")
    else:
        genai.configure(api_key=GEMINI_API_KEY)
        model = genai.GenerativeModel('gemini-pro')
        print("✅ Gemini AI Configured Successfully!")
except Exception as e:
    print(f"❌ Error configuring Gemini: {e}")

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.json
        user_message = data.get('message', '')

        if not user_message:
            return jsonify({"reply": "I didn't hear anything. Could you say that again?"})

        if GEMINI_API_KEY == "YOUR_API_KEY_HERE":
            return jsonify({"reply": "I'm not connected to the brain yet. Please open backend/app.py and add your Gemini API Key."})

        # Generate response from Gemini
        response = model.generate_content(
            f"You are a compassionate, empathetic mental health support assistant named Echo. "
            f"The user is feeling distressed. Respond professionally but warmly. "
            f"Keep your response concise (under 3 sentences) because this is a chat app. "
            f"User says: {user_message}"
        )
        
        reply = response.text.replace('*', '') # Clean up markdown stars
        return jsonify({"reply": reply})

    except Exception as e:
        print(f"Error: {e}")
        # Return the actual error to the user for debugging
        return jsonify({"reply": f"System Error: {str(e)}"}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
