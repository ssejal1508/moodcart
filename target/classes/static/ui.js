const apiBase = '';

const Auth = {
  state: {
    token: null,
    email: null,
    username: null,
    role: null,
  },

  load() {
    try {
      const raw = localStorage.getItem('moodcart_auth');
      if (!raw) return;
      const parsed = JSON.parse(raw);
      if (parsed && parsed.token) {
        this.state = parsed;
      }
    } catch (_) {}
  },

  save() {
    localStorage.setItem('moodcart_auth', JSON.stringify(this.state));
  },

  clear() {
    this.state = { token: null, email: null, username: null, role: null };
    localStorage.removeItem('moodcart_auth');
  },

  isLoggedIn() {
    return !!this.state.token;
  },
};

async function apiRequest(path, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';
  if (Auth.state.token) {
    headers['Authorization'] = `Bearer ${Auth.state.token}`;
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

function setActiveNav() {
  const path = (location.pathname || '').toLowerCase();
  document.querySelectorAll('[data-nav]')?.forEach((a) => {
    const href = (a.getAttribute('href') || '').toLowerCase();
    const isActive = href && (path.endsWith(href) || (href === '/index.html' && (path === '/' || path.endsWith('/'))));
    if (isActive) a.classList.add('active');
    else a.classList.remove('active');
  });
}

function updateHeaderAuth() {
  const statusEl = document.getElementById('auth-status');
  const loginLink = document.getElementById('nav-login');
  const logoutBtn = document.getElementById('btn-logout');

  if (!statusEl) return;

  if (!Auth.isLoggedIn()) {
    statusEl.textContent = 'Not logged in';
    if (logoutBtn) logoutBtn.style.display = 'none';
    if (loginLink) loginLink.style.display = 'inline-flex';
  } else {
    const name = Auth.state.username || Auth.state.email || 'user';
    statusEl.textContent = `Hi, ${name}`;
    if (logoutBtn) logoutBtn.style.display = 'inline-flex';
    if (loginLink) loginLink.style.display = 'none';
  }
}

function wireLogout() {
  const btn = document.getElementById('btn-logout');
  if (!btn) return;
  btn.addEventListener('click', () => {
    Auth.clear();
    updateHeaderAuth();
    // If a page requires auth, send them to auth.
    if (document.body?.dataset?.requiresAuth === 'true') {
      location.href = '/auth.html';
    }
  });
}

function parseTags(product) {
  if (!product || product.tags == null) return [];
  if (Array.isArray(product.tags)) return product.tags;
  return String(product.tags)
    .split(',')
    .map((t) => t.trim())
    .filter(Boolean);
}

function normalizeBoolean(product, keyA, keyB) {
  if (!product) return false;
  const a = product[keyA];
  if (a !== undefined && a !== null) return !!a;
  const b = product[keyB];
  return !!b;
}

function normalizePrice(product) {
  const raw = product?.price;
  const priceNum = typeof raw === 'number' ? raw : parseFloat(raw);
  return Number.isFinite(priceNum) ? priceNum : null;
}

function productCard(product, options = {}) {
  const { canInteract, isSaved } = options;

  const card = document.createElement('div');
  card.className = 'card';

  const productUrl = product?.affiliateUrl || product?.url || product?.productUrl || '';
  if (productUrl) {
    card.title = 'Open product in a new tab';
    card.addEventListener('click', () => window.open(productUrl, '_blank', 'noopener'));
  } else {
    card.style.cursor = 'default';
  }

  if (product?.imageUrl) {
    const img = document.createElement('img');
    img.className = 'card-img';
    img.src = product.imageUrl;
    img.alt = product.title || 'Product image';
    img.loading = 'lazy';
    card.appendChild(img);
  }

  const title = document.createElement('div');
  title.className = 'card-title';
  title.textContent = product?.title || 'Untitled';
  card.appendChild(title);

  if (product?.description) {
    const desc = document.createElement('div');
    desc.className = 'card-desc';
    desc.textContent = product.description;
    card.appendChild(desc);
  }

  const tags = parseTags(product);
  if (tags.length) {
    const tagsEl = document.createElement('div');
    tagsEl.className = 'tags';
    tags.slice(0, 6).forEach((t) => {
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
  const price = normalizePrice(product);
  left.textContent = price != null ? `₹${price.toFixed(2)}` : '';

  const right = document.createElement('div');
  right.style.display = 'flex';
  right.style.gap = '0.5rem';
  right.style.alignItems = 'center';

  const stats = document.createElement('span');
  stats.className = 'badge';
  stats.textContent = `❤ ${product?.likesCount ?? 0} · 💾 ${product?.savesCount ?? 0}`;
  right.appendChild(stats);

  if (canInteract) {
    const likeBtn = document.createElement('button');
    likeBtn.type = 'button';
    likeBtn.className = 'btn-secondary';
    likeBtn.textContent = normalizeBoolean(product, 'likedByCurrentUser', 'liked') ? 'Unlike' : 'Like';
    likeBtn.addEventListener('click', async (e) => {
      e.stopPropagation();
      if (!Auth.isLoggedIn()) {
        alert('Login first to like products.');
        return;
      }
      try {
        const currentlyLiked = normalizeBoolean(product, 'likedByCurrentUser', 'liked');
        if (currentlyLiked) {
          await apiRequest(`/api/products/${product.id}/like`, { method: 'DELETE' });
          product.likedByCurrentUser = false;
          product.liked = false;
          product.likesCount = (product.likesCount || 1) - 1;
        } else {
          await apiRequest(`/api/products/${product.id}/like`, { method: 'POST' });
          product.likedByCurrentUser = true;
          product.liked = true;
          product.likesCount = (product.likesCount || 0) + 1;
        }
        likeBtn.textContent = normalizeBoolean(product, 'likedByCurrentUser', 'liked') ? 'Unlike' : 'Like';
        stats.textContent = `❤ ${product.likesCount ?? 0} · 💾 ${product.savesCount ?? 0}`;
      } catch (err) {
        alert(err?.message || 'Failed to update like.');
      }
    });

    const saveBtn = document.createElement('button');
    saveBtn.type = 'button';
    saveBtn.className = 'btn-primary';
    saveBtn.textContent = isSaved || normalizeBoolean(product, 'savedByCurrentUser', 'saved') ? 'Saved' : 'Save';
    saveBtn.addEventListener('click', async (e) => {
      e.stopPropagation();
      if (!Auth.isLoggedIn()) {
        alert('Login first to save products.');
        return;
      }
      try {
        const currentlySaved = normalizeBoolean(product, 'savedByCurrentUser', 'saved');
        if (isSaved || currentlySaved) {
          alert('Already saved.');
          return;
        }
        await apiRequest('/api/products/save', {
          method: 'POST',
          body: JSON.stringify({ productId: product.id }),
        });
        product.savedByCurrentUser = true;
        product.saved = true;
        product.savesCount = (product.savesCount || 0) + 1;
        saveBtn.textContent = 'Saved';
        stats.textContent = `❤ ${product.likesCount ?? 0} · 💾 ${product.savesCount ?? 0}`;
      } catch (err) {
        alert(err?.message || 'Failed to save product.');
      }
    });

    right.appendChild(likeBtn);
    right.appendChild(saveBtn);
  }

  footer.appendChild(left);
  footer.appendChild(right);
  card.appendChild(footer);

  return card;
}

async function loadTrending(targetId, size = 12) {
  const grid = document.getElementById(targetId);
  if (!grid) return;
  grid.textContent = 'Loading trending…';
  try {
    const data = await apiRequest(`/api/products/trending?page=0&size=${size}`);
    grid.innerHTML = '';
    (data.products || []).forEach((p) => grid.appendChild(productCard(p, { canInteract: true })));
  } catch (err) {
    grid.textContent = err?.message || 'Failed to load trending.';
  }
}

async function loadVibeFeed(query, targetId, titleId) {
  const grid = document.getElementById(targetId);
  const title = document.getElementById(titleId);
  if (title) title.textContent = query ? `Mood Feed – ${query}` : 'Mood Feed';
  if (!grid) return;

  const q = (query || '').trim();
  if (!q) {
    grid.textContent = 'Type a vibe to see results.';
    return;
  }

  grid.textContent = 'Loading feed…';
  try {
    const data = await apiRequest(`/api/moods/vibe-feed?q=${encodeURIComponent(q)}&page=0&size=20`);
    grid.innerHTML = '';
    (data.products || []).forEach((p) => grid.appendChild(productCard(p, { canInteract: true })));
  } catch (err) {
    grid.textContent = err?.message || 'Failed to load feed.';
  }
}

async function loadSaved(targetId, size = 20) {
  const grid = document.getElementById(targetId);
  if (!grid) return;
  if (!Auth.isLoggedIn()) {
    grid.textContent = 'Login to see your saved products.';
    return;
  }

  grid.textContent = 'Loading saved…';
  try {
    const data = await apiRequest(`/api/products/saved?page=0&size=${size}`);
    grid.innerHTML = '';
    (data.products || []).forEach((p) => grid.appendChild(productCard(p, { canInteract: true, isSaved: true })));
  } catch (err) {
    grid.textContent = err?.message || 'Failed to load saved products.';
  }
}

async function loginOrSignup(mode, email, username, password) {
  const body = { email, password };
  if (mode === 'signup' && username) body.username = username;

  const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/signup';
  const data = await apiRequest(endpoint, { method: 'POST', body: JSON.stringify(body) });
  Auth.state = {
    token: data.token,
    email: data.email,
    username: data.username,
    role: data.role,
  };
  Auth.save();
}

function initShell() {
  Auth.load();
  setActiveNav();
  updateHeaderAuth();
  wireLogout();

  const year = document.getElementById('year');
  if (year) year.textContent = new Date().getFullYear();
}

window.MoodCartUI = {
  Auth,
  apiRequest,
  initShell,
  loadTrending,
  loadVibeFeed,
  loadSaved,
  loginOrSignup,
};
