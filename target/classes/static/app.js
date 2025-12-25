const apiBase = '';

let authState = {
  token: null,
  email: null,
  username: null,
  role: null,
};

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem('moodcart_auth');
    if (!raw) return;
    const parsed = JSON.parse(raw);
    if (parsed && parsed.token) {
      authState = parsed;
    }
  } catch (_) {}
}

function saveAuth() {
  localStorage.setItem('moodcart_auth', JSON.stringify(authState));
}

function clearAuth() {
  authState = { token: null, email: null, username: null, role: null };
  localStorage.removeItem('moodcart_auth');
}

function updateAuthStatus() {
  const el = document.getElementById('auth-status');
  const logoutBtn = document.getElementById('btn-logout');
  if (!authState.token) {
    el.textContent = 'Not logged in';
    if (logoutBtn) logoutBtn.style.display = 'none';
  } else {
    el.textContent = `Logged in as ${authState.username || authState.email} (${authState.role})`;
    if (logoutBtn) logoutBtn.style.display = 'inline-flex';
  }
}

async function apiRequest(path, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';
  if (authState.token) {
    headers['Authorization'] = `Bearer ${authState.token}`;
  }
  const res = await fetch(apiBase + path, { ...options, headers });
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const data = await res.json();
      if (data && data.message) msg = data.message;
    } catch (_) {}
    throw new Error(msg);
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return res.json();
  }
  return null;
}

async function handleAuth(mode) {
  const email = document.getElementById('auth-email').value.trim();
  const username = document.getElementById('auth-username').value.trim();
  const password = document.getElementById('auth-password').value.trim();
  const errEl = document.getElementById('auth-error');
  errEl.textContent = '';

  if (!email || !password) {
    errEl.textContent = 'Email and password are required.';
    return;
  }

  const body = { email, password };
  if (mode === 'signup' && username) {
    body.username = username;
  }

  try {
    const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/signup';
    const data = await apiRequest(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    authState = {
      token: data.token,
      email: data.email,
      username: data.username,
      role: data.role,
    };
    saveAuth();
    updateAuthStatus();
    await loadSavedProducts();
  } catch (e) {
    errEl.textContent = e.message || 'Auth failed.';
  }
}

async function loadMoods() {
  const container = document.getElementById('moods-list');
  container.innerHTML = 'Loading moods...';
  try {
    const moods = await apiRequest('/api/moods');
    container.innerHTML = '';
    moods.forEach((mood) => {
      const chip = document.createElement('button');
      chip.type = 'button';
      chip.className = 'chip';
      chip.textContent = mood.name;
      chip.onclick = () => {
        document
          .querySelectorAll('#moods-list .chip')
          .forEach((c) => c.classList.remove('active'));
        chip.classList.add('active');
        loadMoodFeed(mood.id, mood.name);
      };
      container.appendChild(chip);
    });
  } catch (e) {
    container.textContent = e.message || 'Failed to load moods.';
  }
}

function productCard(product, options = {}) {
  const { canInteract, isSaved } = options;
  const card = document.createElement('div');
  card.className = 'card';

  const title = document.createElement('div');
  title.className = 'card-title';
  title.textContent = product.title;
  card.appendChild(title);

  if (product.description) {
    const desc = document.createElement('div');
    desc.className = 'card-desc';
    desc.textContent = product.description;
    card.appendChild(desc);
  }

  if (product.tags && product.tags.length) {
    const tagsEl = document.createElement('div');
    tagsEl.className = 'card-tags';
    product.tags.forEach((t) => {
      const tag = document.createElement('span');
      tag.className = 'tag';
      tag.textContent = t;
      tagsEl.appendChild(tag);
    });
    card.appendChild(tagsEl);
  }

  const footer = document.createElement('div');
  footer.className = 'card-footer';

  const left = document.createElement('div');
  left.className = 'price';
  left.textContent = product.price != null ? `₹${product.price.toFixed(2)}` : '';

  const right = document.createElement('div');
  right.className = 'interactions';

  const stats = document.createElement('span');
  stats.className = 'badge';
  stats.textContent = `❤ ${product.likesCount ?? 0} · 💾 ${product.savesCount ?? 0}`;
  right.appendChild(stats);

  if (canInteract) {
    const likeBtn = document.createElement('button');
    likeBtn.type = 'button';
    likeBtn.className = 'btn-secondary';
    likeBtn.textContent = product.likedByCurrentUser ? 'Unlike' : 'Like';
    likeBtn.onclick = async () => {
      if (!authState.token) {
        alert('Login first to like products.');
        return;
      }
      try {
        if (product.likedByCurrentUser) {
          await apiRequest(`/api/products/${product.id}/like`, { method: 'DELETE' });
          product.likedByCurrentUser = false;
          product.likesCount = (product.likesCount || 1) - 1;
        } else {
          await apiRequest(`/api/products/${product.id}/like`, { method: 'POST' });
          product.likedByCurrentUser = true;
          product.likesCount = (product.likesCount || 0) + 1;
        }
        likeBtn.textContent = product.likedByCurrentUser ? 'Unlike' : 'Like';
        stats.textContent = `❤ ${product.likesCount ?? 0} · 💾 ${product.savesCount ?? 0}`;
      } catch (e) {
        alert(e.message || 'Failed to update like.');
      }
    };
    right.appendChild(likeBtn);

    const saveBtn = document.createElement('button');
    saveBtn.type = 'button';
    saveBtn.className = 'primary';
    saveBtn.textContent = isSaved || product.savedByCurrentUser ? 'Saved' : 'Save';
    saveBtn.onclick = async () => {
      if (!authState.token) {
        alert('Login first to save products.');
        return;
      }
      try {
        if (isSaved || product.savedByCurrentUser) {
          alert('Already saved.');
          return;
        }
        const dto = { productId: product.id };
        await apiRequest('/api/products/save', {
          method: 'POST',
          body: JSON.stringify(dto),
        });
        product.savedByCurrentUser = true;
        product.savesCount = (product.savesCount || 0) + 1;
        saveBtn.textContent = 'Saved';
        stats.textContent = `❤ ${product.likesCount ?? 0} · 💾 ${product.savesCount ?? 0}`;
      } catch (e) {
        alert(e.message || 'Failed to save product.');
      }
    };
    right.appendChild(saveBtn);
  }

  footer.appendChild(left);
  footer.appendChild(right);
  card.appendChild(footer);
  return card;
}

async function loadTrending() {
  const grid = document.getElementById('trending-grid');
  grid.innerHTML = 'Loading trending products...';
  try {
    const data = await apiRequest('/api/products/trending?page=0&size=12');
    grid.innerHTML = '';
    (data.products || []).forEach((p) => {
      grid.appendChild(productCard(p, { canInteract: true }));
    });
  } catch (e) {
    grid.textContent = e.message || 'Failed to load trending.';
  }
}

async function loadMoodFeed(moodId, moodName) {
  const title = document.getElementById('mood-feed-title');
  const grid = document.getElementById('mood-feed-grid');
  title.textContent = `Mood Feed – ${moodName}`;
  grid.innerHTML = 'Loading mood feed...';
  try {
    const data = await apiRequest(`/api/moods/${moodId}/feed?page=0&size=20`);
    grid.innerHTML = '';
    (data.products || []).forEach((p) => {
      grid.appendChild(productCard(p, { canInteract: true }));
    });
  } catch (e) {
    grid.textContent = e.message || 'Failed to load mood feed.';
  }
}

async function loadSavedProducts() {
  const grid = document.getElementById('saved-grid');
  if (!authState.token) {
    grid.textContent = 'Login to see your saved products.';
    return;
  }
  grid.innerHTML = 'Loading saved products...';
  try {
    const data = await apiRequest('/api/products/saved?page=0&size=20');
    grid.innerHTML = '';
    (data.products || []).forEach((p) => {
      grid.appendChild(productCard(p, { canInteract: true, isSaved: true }));
    });
  } catch (e) {
    grid.textContent = e.message || 'Failed to load saved products.';
  }
}

function init() {
  document.getElementById('year').textContent = new Date().getFullYear();

  loadStoredAuth();
  updateAuthStatus();

  document.getElementById('btn-login').addEventListener('click', () => handleAuth('login'));
  document.getElementById('btn-signup').addEventListener('click', () => handleAuth('signup'));
  document.getElementById('btn-logout').addEventListener('click', () => {
    clearAuth();
    updateAuthStatus();
    document.getElementById('saved-grid').textContent = 'Login to see your saved products.';
  });
  document.getElementById('btn-load-saved').addEventListener('click', loadSavedProducts);

  loadMoods();
  loadTrending();
  if (authState.token) {
    loadSavedProducts();
  }
}

document.addEventListener('DOMContentLoaded', init);
