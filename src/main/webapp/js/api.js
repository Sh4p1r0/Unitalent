// Pequeña utilería para llamar a los servlets del backend en formato JSON.
const Api = {
  async request(method, url, body) {
    const opts = {
      method,
      headers: { 'Content-Type': 'application/json; charset=UTF-8' },
      credentials: 'same-origin' // envía la cookie de sesión (JSESSIONID)
    };
    if (body !== undefined) opts.body = JSON.stringify(body);

    const res = await fetch(url, opts);
    let data = null;
    try { data = await res.json(); } catch (e) { /* respuesta vacía */ }

    if (!res.ok) {
      const message = (data && data.error) ? data.error : 'Ocurrió un error inesperado.';
      throw new Error(message);
    }
    return data;
  },
  get(url) { return this.request('GET', url); },
  post(url, body) { return this.request('POST', url, body); },
  put(url, body) { return this.request('PUT', url, body); },
  del(url) { return this.request('DELETE', url); }
};

// Verifica sesión activa; si no hay sesión válida, redirige al login.
async function requireSession(rolEsperado) {
  try {
    const data = await Api.get('/UniTalent/api/login');
    if (!data.ok) { window.location.href = '/UniTalent/login.html'; return null; }
    if (rolEsperado && data.rol !== rolEsperado) {
      window.location.href = '/UniTalent/login.html';
      return null;
    }
    document.querySelectorAll('[data-user-name]').forEach(el => el.textContent = data.nombre);
    document.querySelectorAll('[data-user-initial]').forEach(el => el.textContent = (data.nombre || '?').charAt(0).toUpperCase());
    return data;
  } catch (e) {
    window.location.href = '/UniTalent/login.html';
    return null;
  }
}

async function logout() {
  try { await Api.post('/UniTalent/api/logout'); } catch (e) {}
  window.location.href = '/UniTalent/login.html';
}

function showMsg(elId, text, type) {
  const el = document.getElementById(elId);
  el.textContent = text;
  el.className = 'msg ' + type;
}

function fixEncoding(str) {
  if (!str) return '';
  return str.toString()
    .replace(/Ingenier[^\s]+ de Sistemas/gi, 'Ingeniería de Sistemas')
    .replace(/ÃƒÂƒÃ‚Â­/g, 'í')
    .replace(/ÃƒÂ­/g, 'í')
    .replace(/Ã‚Â/g, '')
    .replace(/ÃÂ\s*ÃÂ/g, '')
    .replace(/ÃÂ/g, '')
    .replace(/Ã¡/g, 'á')
    .replace(/Ã©/g, 'é')
    .replace(/Ã­/g, 'í')
    .replace(/Ã\u00ad/g, 'í')
    .replace(/Ã³/g, 'ó')
    .replace(/Ãº/g, 'ú')
    .replace(/Ã±/g, 'ñ')
    .replace(/Ã/g, 'Á')
    .replace(/Ã‰/g, 'É')
    .replace(/Ã/g, 'Í')
    .replace(/Ã“/g, 'Ó')
    .replace(/Ãš/g, 'Ú')
    .replace(/Ã‘/g, 'Ñ');
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return fixEncoding(str.toString())
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
