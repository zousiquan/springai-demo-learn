const messagesDiv = document.getElementById('messages');
const userInput = document.getElementById('userInput');
const sendBtn = document.getElementById('sendBtn');
const ragToggle = document.getElementById('ragToggle');
const fileUpload = document.getElementById('fileUpload');
const uploadProgress = document.getElementById('uploadProgress');
const uploadBar = document.getElementById('uploadBar');
const kbList = document.getElementById('kbList');
const uploadModal = document.getElementById('uploadModal');
const selectedFiles = document.getElementById('selectedFiles');
const startUploadBtn = document.getElementById('startUploadBtn');

// 用于存储当前正在处理的流式请求，以便支持中断功能
let currentStreamingRequest = null;

// 消息计数器，用于唯一标识每条消息
let messageCounter = 0;

let currentKnowledgeBase = 'coffee_collection';
let knowledgeBases = {
    'coffee_collection': { name: '默认知识库', description: '系统默认知识库' }
};
let selectedFilesList = [];

// 会话ID管理
let conversationId = null;

// 初始化会话ID
function initConversationId() {
    // 检查本地存储中是否已有会话ID
    conversationId = localStorage.getItem('conversationId');
    if (!conversationId) {
        // 生成新的会话ID
        conversationId = generateConversationId();
        // 保存到本地存储
        localStorage.setItem('conversationId', conversationId);
    }
}

// 生成会话ID
function generateConversationId() {
    // 生成格式：时间戳_随机数
    return `${Date.now()}_${Math.floor(Math.random() * 1000000)}`;
}

// 新建会话
function newConversation() {
    // 清空聊天记录
    messagesDiv.innerHTML = '';
    // 生成新的会话ID
    conversationId = generateConversationId();
    // 保存到本地存储
    localStorage.setItem('conversationId', conversationId);
    // 添加系统欢迎消息
    appendMessage('system', '欢迎使用智能聊天助手！你可以开始提问了。');
    // 聚焦到输入框
    userInput.focus();
}

// 初始化会话ID
initConversationId();

// 初始化消息区域
appendMessage('system', '欢迎使用智能聊天助手！你可以开始提问了。');

// 加载知识库列表
loadKnowledgeBases();

/**
 * 添加消息到聊天界面
 * @param {string} sender 发送者（user, ai, system）
 * @param {string} text 消息内容
 * @param {boolean} isStreaming 是否为流式消息
 * @returns {Object} 消息元素对象，用于流式更新
 */
function appendMessage(sender, text, isStreaming = false) {
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${sender}`;
    
    // 生成唯一的消息ID
    const messageId = `msg_${Date.now()}_${messageCounter++}`;
    msgDiv.setAttribute('id', messageId);
    
    let bubbleContent = '';
    let senderLabel = '';
    
    switch(sender) {
        case 'user':
            senderLabel = '你';
            bubbleContent = `<div class="message-sender">${senderLabel}</div>${text}`;
            msgDiv.innerHTML = `<div class="message-bubble">${bubbleContent}</div>`;
            break;
        case 'ai':
            senderLabel = 'AI';
            if (isStreaming) {
                // 流式消息，添加等待动效和中断按钮
                bubbleContent = `
                    <div class="message-sender">${senderLabel}</div>
                    <div class="message-content">${text}</div>
                    <div class="typing-indicator">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                    <div class="message-actions">
                        <button class="btn-small interrupt" onclick="interruptStream('${messageId}')">中断</button>
                    </div>
                `;
            } else {
                // 完整消息，添加复制和重新生成按钮
                bubbleContent = `
                    <div class="message-sender">${senderLabel}</div>
                    <div class="message-content">${text}</div>
                    <div class="message-actions">
                        <button class="btn-small copy" onclick="copyMessage('${messageId}')">复制</button>
                        <button class="btn-small regenerate" onclick="regenerateMessage('${messageId}')">重新生成</button>
                    </div>
                `;
            }
            msgDiv.innerHTML = `<div class="message-bubble">${bubbleContent}</div>`;
            break;
        case 'system':
            senderLabel = '系统提示';
            bubbleContent = text;
            msgDiv.innerHTML = `<div class="message-bubble">${bubbleContent}</div>`;
            break;
        default:
            senderLabel = sender;
            bubbleContent = `<div class="message-sender">${senderLabel}</div>${text}`;
            msgDiv.innerHTML = `<div class="message-bubble">${bubbleContent}</div>`;
    }
    
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    
    return {
        element: msgDiv,
        messageId: messageId
    };
}

/**
 * 更新流式消息内容
 * @param {string} messageId 消息ID
 * @param {string} newContent 新的消息内容
 * @param {boolean} isComplete 是否完成
 */
function updateStreamingMessage(messageId, newContent, isComplete = false) {
    const msgDiv = document.getElementById(messageId);
    if (!msgDiv) return;
    
    const contentDiv = msgDiv.querySelector('.message-content');
    const typingIndicator = msgDiv.querySelector('.typing-indicator');
    const actionsDiv = msgDiv.querySelector('.message-actions');
    
    if (contentDiv) {
        contentDiv.textContent = newContent;
    }
    
    if (isComplete) {
        // 消息完成，移除打字指示器和中断按钮，添加复制和重新生成按钮
        if (typingIndicator) {
            typingIndicator.remove();
        }
        
        if (actionsDiv) {
            actionsDiv.innerHTML = `
                <button class="btn-small copy" onclick="copyMessage('${messageId}')">复制</button>
                <button class="btn-small regenerate" onclick="regenerateMessage('${messageId}')">重新生成</button>
            `;
        }
    }
    
    // 滚动到底部
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

/**
 * 中断当前流式请求
 * @param {string} messageId 消息ID
 */
function interruptStream(messageId) {
    if (currentStreamingRequest) {
        // 取消当前的fetch请求
        currentStreamingRequest.cancel();
        currentStreamingRequest = null;
        
        // 更新消息状态
        updateStreamingMessage(messageId, document.getElementById(messageId).querySelector('.message-content').textContent, true);
        
        // 恢复发送按钮
        sendBtn.disabled = false;
        userInput.focus();
    }
}

/**
 * 复制消息内容到剪贴板
 * @param {string} messageId 消息ID
 */
function copyMessage(messageId) {
    const msgDiv = document.getElementById(messageId);
    const contentDiv = msgDiv.querySelector('.message-content');
    if (contentDiv) {
        navigator.clipboard.writeText(contentDiv.textContent)
            .then(() => {
                // 显示复制成功提示
                const originalText = contentDiv.textContent;
                contentDiv.textContent = '已复制到剪贴板！';
                setTimeout(() => {
                    contentDiv.textContent = originalText;
                }, 1500);
            })
            .catch(err => {
                console.error('复制失败:', err);
            });
    }
}

/**
 * 重新生成消息
 * @param {string} messageId 消息ID
 */
function regenerateMessage(messageId) {
    const msgDiv = document.getElementById(messageId);
    // 找到对应的用户消息
    const userMessages = messagesDiv.querySelectorAll('.message.user');
    let userMessage = null;
    let currentIndex = Array.from(messagesDiv.children).indexOf(msgDiv);
    
    // 向上查找最近的用户消息
    for (let i = currentIndex - 1; i >= 0; i--) {
        const msg = messagesDiv.children[i];
        if (msg.classList.contains('user')) {
            userMessage = msg;
            break;
        }
    }
    
    if (userMessage) {
        // 获取用户消息内容
        const userContent = userMessage.querySelector('.message-content') || userMessage.querySelector('.message-bubble');
        const userText = userContent.textContent.replace('你：', '').trim();
        
        // 删除当前AI消息和之后的所有消息
        while (messagesDiv.lastChild !== userMessage) {
            messagesDiv.removeChild(messagesDiv.lastChild);
        }
        
        // 重新发送消息
        sendMessage();
    }
}

function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

function toggleRagMode() {
    const isChecked = ragToggle.checked;
    
    if (isChecked) {
        appendMessage('system', '知识库模式已开启，你可以上传文件到当前选中的知识库。');
    } else {
        appendMessage('system', '知识库模式已关闭，AI将直接回答问题。');
    }
}

// 打开新建知识库模态框
function createNewKnowledgeBase() {
    const modal = document.getElementById('createKbModal');
    modal.style.display = 'flex';
}

// 关闭新建知识库模态框
function closeCreateKbModal() {
    const modal = document.getElementById('createKbModal');
    modal.style.display = 'none';
    // 清空表单
    document.getElementById('kbName').value = '';
    document.getElementById('kbDescription').value = '';
}

// 确认创建知识库
function createKnowledgeBaseConfirm() {
    const name = document.getElementById('kbName').value.trim();
    const description = document.getElementById('kbDescription').value.trim();
    
    if (!name) {
        alert('请输入知识库名称');
        return;
    }
    
    // 生成知识库ID
    const kbId = 'kb_' + Date.now();
    
    // 调用后端API创建知识库
    fetch('/rag/knowledge-bases', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            id: kbId,
            name: name,
            description: description
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            knowledgeBases[kbId] = { name, description };
            renderKnowledgeBases();
            selectKnowledgeBase(kbId);
            appendMessage('system', `已创建新知识库：${name}`);
            closeCreateKbModal();
        } else {
            appendMessage('system', '创建知识库失败：' + data.message);
        }
    })
    .catch(error => {
        appendMessage('system', '创建知识库失败：' + error.message);
        console.error('Create knowledge base error:', error);
    });
}



function deleteKnowledgeBase(kbId) {
    if (kbId === 'default') {
        alert('默认知识库不能删除！');
        return;
    }
    
    if (confirm('确定要删除这个知识库吗？')) {
        // 调用后端API删除知识库
        fetch(`/rag/knowledge-bases/${kbId}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                delete knowledgeBases[kbId];
                renderKnowledgeBases();
                selectKnowledgeBase('coffee_collection');
                appendMessage('system', data.message);
            } else {
                appendMessage('system', '删除知识库失败：' + data.message);
            }
        })
        .catch(error => {
            appendMessage('system', '删除知识库失败：' + error.message);
            console.error('Delete knowledge base error:', error);
        });
    }
}

function selectKnowledgeBase(kbId) {
    // 移除所有选中状态
    document.querySelectorAll('.kb-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // 添加当前选中状态
    const selectedItem = document.querySelector(`[data-kb-id="${kbId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
        currentKnowledgeBase = kbId;
        appendMessage('system', `已切换到知识库：${knowledgeBases[kbId].name}`);
    }
}

function renderKnowledgeBases() {
    kbList.innerHTML = '';
    
    Object.entries(knowledgeBases).forEach(([kbId, kb]) => {
        const kbItem = document.createElement('div');
        kbItem.className = `kb-item ${kbId === currentKnowledgeBase ? 'active' : ''}`;
        kbItem.setAttribute('data-kb-id', kbId);
        kbItem.onclick = () => selectKnowledgeBase(kbId);
        
        kbItem.innerHTML = `
            <div class="kb-name">${kb.name}</div>
            <div class="kb-description">${kb.description}</div>
            <div class="kb-actions">
                <button class="btn-small upload" onclick="event.stopPropagation(); openUploadModal('${kbId}')">上传</button>
                <button class="btn-small delete" onclick="event.stopPropagation(); deleteKnowledgeBase('${kbId}')">删除</button>
            </div>
        `;
        
        kbList.appendChild(kbItem);
    });
}

// 加载知识库列表
function loadKnowledgeBases() {
    fetch('/rag/knowledge-bases')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                knowledgeBases = {};
                data.data.forEach(kb => {
                    knowledgeBases[kb.id] = {
                        name: kb.name,
                        description: kb.description
                    };
                });
                renderKnowledgeBases();
            }
        })
        .catch(error => {
            console.error('Failed to load knowledge bases:', error);
        });
}

// 打开上传文件模态框
function openUploadModal(kbId) {
    if (kbId) {
        currentKnowledgeBase = kbId;
        selectKnowledgeBase(kbId);
    }
    selectedFilesList = [];
    selectedFiles.innerHTML = '';
    startUploadBtn.disabled = true;
    // 使用flex布局显示模态框，确保居中
    uploadModal.style.display = 'flex';
}

// 关闭上传文件模态框
function closeUploadModal() {
    uploadModal.style.display = 'none';
    fileUpload.value = '';
    selectedFilesList = [];
    selectedFiles.innerHTML = '';
    startUploadBtn.disabled = true;
}

// 处理文件选择
function handleFileSelect(event) {
    const files = event.target.files;
    selectedFilesList = Array.from(files);
    
    // 渲染选中的文件列表
    renderSelectedFiles();
    
    // 启用上传按钮
    startUploadBtn.disabled = selectedFilesList.length === 0;
}

// 渲染选中的文件列表
function renderSelectedFiles() {
    selectedFiles.innerHTML = '';
    
    selectedFilesList.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';
        
        fileItem.innerHTML = `
            <div class="file-info">
                <div class="file-name">${file.name}</div>
                <div class="file-size">${(file.size / 1024 / 1024).toFixed(2)} MB</div>
            </div>
            <button class="remove-file" onclick="removeFile(${index})">移除</button>
        `;
        
        selectedFiles.appendChild(fileItem);
    });
}

// 移除选中的文件
function removeFile(index) {
    selectedFilesList.splice(index, 1);
    renderSelectedFiles();
    startUploadBtn.disabled = selectedFilesList.length === 0;
}

function uploadFile() {
    if (selectedFilesList.length === 0) {
        alert('请选择要上传的文件');
        return;
    }
    
    // 显示选择的文件信息
    let fileInfo = '已选择文件：\n';
    for (let i = 0; i < selectedFilesList.length; i++) {
        fileInfo += `${selectedFilesList[i].name} (${(selectedFilesList[i].size / 1024 / 1024).toFixed(2)} MB)\n`;
    }
    appendMessage('system', fileInfo);
    
    const formData = new FormData();
    selectedFilesList.forEach(file => {
        formData.append('file', file);
    });
    formData.append('tag', '通用文档');
    formData.append('collectionName', currentKnowledgeBase);
    
    showUploadProgress();
    
    fetch('/rag/upload-file', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(result => {
        hideUploadProgress();
        appendMessage('system', result);
        closeUploadModal();
    })
    .catch(error => {
        hideUploadProgress();
        appendMessage('system', '文件上传失败');
        console.error('Upload error:', error);
    });
}

/**
 * 发送消息并处理流式响应
 */
function sendMessage() {
    const text = userInput.value.trim();
    if (!text) return;
    
    // 添加用户消息
    appendMessage('user', text);
    userInput.value = '';
    
    // 将发送按钮切换为中断状态
    sendBtn.classList.add('interrupt');
    sendBtn.innerHTML = `<span class="send-icon">→</span><span class="interrupt-icon">×</span>`;
    
    // 修改按钮的点击事件为中断功能
    sendBtn.onclick = function() {
        interruptStream(messageId);
    };
    
    // 添加初始的AI流式消息，带有等待动效
    const aiMessage = appendMessage('ai', '', true);
    const messageId = aiMessage.messageId;
    
    // 根据是否启用知识库模式选择不同的API，并添加会话ID参数
    const apiUrl = ragToggle.checked 
        ? `/rag/ask?question=${encodeURIComponent(text)}&collectionName=${currentKnowledgeBase}&conversationId=${conversationId}` 
        : `/text?message=${encodeURIComponent(text)}&conversationId=${conversationId}`;
    
    // 模拟流式输出的内容
    let simulatedResponse = "";
    
    // 创建一个AbortController，用于支持中断功能
    const controller = new AbortController();
    const { signal } = controller;
    currentStreamingRequest = {
        cancel: () => controller.abort(),
        messageId: messageId
    };
    
    // 使用fetch API发送请求
    fetch(apiUrl, {
        signal: signal
    })
    .then(res => {
        if (!res.ok) {
            throw new Error('Network response was not ok');
        }
        return res.text();
    })
    .then(data => {
        // 模拟流式输出
        simulatedResponse = data;
        let index = 0;
        
        // 每次输出多个字符，模拟更自然的打字效果
        const typingInterval = setInterval(() => {
            if (index < simulatedResponse.length) {
                // 随机输出1-4个字符，使打字效果更自然
                const charsToAdd = Math.min(Math.floor(Math.random() * 4) + 1, simulatedResponse.length - index);
                const newContent = simulatedResponse.substring(0, index + charsToAdd);
                updateStreamingMessage(messageId, newContent);
                index += charsToAdd;
            } else {
                // 完成流式输出
                clearInterval(typingInterval);
                updateStreamingMessage(messageId, simulatedResponse, true);
                resetSendButton();
            }
        }, 50); // 每50毫秒显示一批字符
        
        return new Promise(resolve => {
            // 确保在流式输出完成后resolve
            setTimeout(() => resolve(), simulatedResponse.length * 50 + 100);
        });
    })
    .catch(error => {
        if (error.name === 'AbortError') {
            // 请求被中断，不显示错误信息
            console.log('Request aborted');
        } else {
            // 其他错误，显示错误信息
            updateStreamingMessage(messageId, '请求失败，请稍后重试。', true);
            console.error('Error:', error);
        }
        resetSendButton();
    });
}

/**
 * 重置发送按钮为初始状态
 */
function resetSendButton() {
    // 恢复发送按钮的初始状态
    sendBtn.classList.remove('interrupt');
    sendBtn.innerHTML = `<span class="send-icon">→</span><span class="interrupt-icon">×</span>`;
    
    // 恢复按钮的点击事件为发送功能
    sendBtn.onclick = sendMessage;
    sendBtn.disabled = false;
    userInput.focus();
    
    // 清除当前流式请求
    currentStreamingRequest = null;
}

/**
 * 中断当前流式请求
 * @param {string} messageId 消息ID
 */
function interruptStream(messageId) {
    if (currentStreamingRequest) {
        // 取消当前的fetch请求
        currentStreamingRequest.cancel();
        
        // 更新消息状态
        updateStreamingMessage(messageId, document.getElementById(messageId).querySelector('.message-content').textContent, true);
        
        // 重置发送按钮
        resetSendButton();
    }
}

function showUploadProgress() {
    const progress = document.getElementById('uploadProgress');
    const bar = document.getElementById('uploadBar');
    progress.style.display = 'block';
    bar.style.width = '0%';
    
    // 模拟上传进度
    let progressValue = 0;
    const interval = setInterval(() => {
        progressValue += Math.random() * 10;
        if (progressValue >= 90) {
            clearInterval(interval);
        }
        bar.style.width = progressValue + '%';
    }, 200);
}

function hideUploadProgress() {
    const progress = document.getElementById('uploadProgress');
    const bar = document.getElementById('uploadBar');
    bar.style.width = '100%';
    setTimeout(() => {
        progress.style.display = 'none';
    }, 500);
}

// 点击模态框外部关闭模态框
window.onclick = function(event) {
    // 关闭上传文件模态框
    if (event.target === uploadModal) {
        closeUploadModal();
    }
    
    // 关闭新建知识库模态框
    if (event.target === document.getElementById('createKbModal')) {
        closeCreateKbModal();
    }
}

// 初始化
renderKnowledgeBases();