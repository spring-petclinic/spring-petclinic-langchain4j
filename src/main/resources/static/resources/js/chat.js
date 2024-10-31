<!-- JavaScript for handling chatbox interaction -->

<!-- Authors : Odedia Shopen, Antoine Rey -->
function displayMessage(message, elements) {
    let {chatMessages, messageElement} = elements;
    // Convert Markdown to HTML
    // May interpret bullet syntax like
    // 1. **Betty Davis**
    messageElement.innerHTML = marked.parse(message);

    // Scroll to the bottom of the chatbox to show the latest message
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function prepareMessage(type) {
    const chatMessages = document.getElementById('chatbox-messages');
    const messageElement = document.createElement('div');
    messageElement.classList.add('chat-bubble', type);
    chatMessages.appendChild(messageElement);
    return {chatMessages, messageElement};
}

function toggleChatbox() {
    const chatbox = document.getElementById('chatbox');
    const chatboxContent = document.getElementById('chatbox-content');

    if (chatbox.classList.contains('minimized')) {
        chatbox.classList.remove('minimized');
        chatboxContent.style.height = '400px'; // Set to initial height when expanded
    } else {
        chatbox.classList.add('minimized');
        chatboxContent.style.height = '40px'; // Set to minimized height
    }
}

async function sendMessage() {
    const query = document.getElementById('chatbox-input').value;

    // Only send if there's a message
    if (!query.trim()) return;

    // Clear the input field after sending the message
    document.getElementById('chatbox-input').value = '';

    // Display user message in the chat box
    const userElements = prepareMessage("user");
    displayMessage(query, userElements);

    // Retrieve or create a UserID as a UUID v4
    let userId = sessionStorage.getItem('userId');
    if (!userId) {
        userId = uuidv4();
        sessionStorage.setItem('userId', userId);
    }

    // We'll start by using fetch to initiate a POST request to our SSE endpoint.
    // This endpoint is configured to send multiple messages, with the response header Content-Type: text/event-stream.
    let response = await fetch('/chat/' + userId, {
        method: 'POST',
        headers: {
            'Accept': 'text/event-stream',
            'Content-Type': 'application/json',
            'Cache-Control': 'no-cache'
        },
        body: JSON.stringify(query)
    });

    if (response.ok) {
        await displayBotReply(response);
    } else {
        const botElements = prepareMessage('bot');
        displayMessage('Unexpected server error', botElements);
    }

}


async function displayBotReply(response) {
    // Instantiate a reader to process each network request as it arrives from the server.
    const reader = response.body?.getReader();

    // Set up a loop to keep receiving messages until the done signal is triggered.
    // Within this loop, update your frontend application with the incoming SSE messages.
    const botElements = prepareMessage('bot');
    let fullReply = "";
    while (true) {
        const {value, done} = await reader.read();
        const chars = new TextDecoder().decode(value);
        if (done) {
            break;
        }
        const dataArray = chars.trim().split("\n\n");
        const jsonObjects = dataArray.map((data) => {
            const jsonString = data.includes("data:") ? data.substring("data:".length) : data;
            if (jsonString.length === 0) {
                return null;
            }
            return JSON.parse(jsonString);
        }).filter(obj => obj !== null);
        jsonObjects.forEach((item) => {
            fullReply += item.t.replaceAll('<br>', '\n');
        });
        displayMessage(fullReply, botElements);
    }
}

function handleKeyPress(event) {
    if (event.key === "Enter") {
        event.preventDefault(); // Prevents adding a newline
        sendMessage(); // Send the message when Enter is pressed
    }
}

// Save chat messages to sessionStorage
function saveChatMessages() {
    const messages = document.getElementById('chatbox-messages').innerHTML;
    sessionStorage.setItem('chatMessages', messages);
}

// Load chat messages from sessionStorage
function loadChatMessages() {
    const messages = sessionStorage.getItem('chatMessages');
    if (messages) {
        document.getElementById('chatbox-messages').innerHTML = messages;
        document.getElementById('chatbox-messages').scrollTop = document.getElementById('chatbox-messages').scrollHeight;
    }
}

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
        .replace(/[xy]/g, function (c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
}
