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

let currentKnowledgeBase = 'coffee_collection';
let knowledgeBases = {
    'coffee_collection': { name: '默认知识库', description: '系统默认知识库' }
};
let selectedFilesList = [];

// 初始化消息区域
appendMessage('system', '欢迎使用智能聊天助手！你可以开始提问了。');

// 加载知识库列表
loadKnowledgeBases();

function appendMessage(sender, text) {
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${sender}`;
    
    let bubbleContent = '';
    let senderLabel = '';
    
    switch(sender) {
        case 'user':
            senderLabel = '你';
            bubbleContent = `<div class="message-sender">${senderLabel}</div>${text}`;
            break;
        case 'ai':
            senderLabel = 'AI';
            bubbleContent = `<div class="message-sender">${senderLabel}</div>${text}`;
            break;
        case 'system':
            senderLabel = '系统提示';
            bubbleContent = text;
            break;
        default:
            senderLabel = sender;
            bubbleContent = `<div class="message-sender">${senderLabel}</div>${text}`;
    }
    
    msgDiv.innerHTML = `<div class="message-bubble">${bubbleContent}</div>`;
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
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

function sendMessage() {
    const text = userInput.value.trim();
    if (!text) return;
    
    appendMessage('user', text);
    userInput.value = '';
    sendBtn.disabled = true;
    
    // 根据是否启用知识库模式选择不同的API
    const apiUrl = ragToggle.checked 
        ? `/rag/ask?question=${encodeURIComponent(text)}&collectionName=${currentKnowledgeBase}` 
        : `/text?message=${encodeURIComponent(text)}`;
    
    fetch(apiUrl)
        .then(res => res.text())
        .then(data => {
            appendMessage('ai', data);
        })
        .catch(() => {
            appendMessage('ai', '请求失败，请稍后重试。');
        })
        .finally(() => {
            sendBtn.disabled = false;
            userInput.focus();
        });
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