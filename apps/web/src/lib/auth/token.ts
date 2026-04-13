const TOKEN_KEY = "auth_token";
const EMAIL_KEY = "auth_email";

export const tokenStore = {
  get(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(TOKEN_KEY);
  },

  set(token: string, email: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(EMAIL_KEY, email);
  },

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(EMAIL_KEY);
  },

  getEmail(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(EMAIL_KEY);
  },

  isAuthenticated(): boolean {
    const token = this.get();
    if (!token) return false;

    // JWT payload decode — imza doğrulaması yapılmaz, sadece exp okunur.
    // JWT base64url kullanır: "+" yerine "-", "/" yerine "_", padding yok.
    // atob() standart base64 bekler — dönüştürmek gerekir.
    try {
      const base64url = token.split(".")[1];
      if (!base64url) return false;
      const base64 = base64url.replace(/-/g, "+").replace(/_/g, "/");
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
      const payload = JSON.parse(atob(padded));
      return typeof payload.exp === "number" && payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  },
};
