# Portfolio Web

Next.js 15 frontend for the interior design portfolio platform.

---

## What This App Does

- **Public site** — portfolio homepage, projects listing, project detail pages, contact form
- **Admin panel** — JWT-protected dashboard for managing projects, uploading images, reading contact messages

---

## Pages

### Public

| Route | Description |
|---|---|
| `/` | Homepage — hero, featured projects, contact form |
| `/projects` | All published projects grid |
| `/projects/[slug]` | Project detail page |

### Admin

| Route | Description |
|---|---|
| `/admin/login` | Login page |
| `/admin` | Dashboard overview |
| `/admin/projects` | Projects list with edit/delete |
| `/admin/projects/new` | Create new project |
| `/admin/projects/[id]/edit` | Edit existing project |
| `/admin/messages` | Contact messages inbox |

---

## Stack

- **Framework:** Next.js 15 (App Router)
- **Styling:** Tailwind CSS
- **HTTP client:** Axios
- **Auth:** JWT stored in localStorage, decoded client-side for expiry check

---

## Local Development

### Prerequisites

- Node.js 18+
- Backend running at `http://localhost:8080` (see `apps/api/README.md`)

### Start

```bash
cd apps/web
npm install
npm run dev
```

App runs at `http://localhost:3000`.

### Environment

The API base URL defaults to `http://localhost:8080`. Override via:

```
NEXT_PUBLIC_API_URL=https://your-api.com
```

---

## Auth Flow

1. `POST /api/v1/auth/login` → receives JWT token
2. Token stored in `localStorage` as `auth_token`
3. Every request includes `Authorization: Bearer <token>` via Axios interceptor
4. On 401 response: token cleared, user redirected to `/admin/login`
5. Token expiry checked locally (JWT `exp` claim) before each page load

---

## Image Upload Flow

1. User selects a file in `ImageUploader`
2. `POST /api/v1/admin/media/upload` → returns `{ id, url }`
3. On project **create**: `coverImageId` sent in the create request body
4. On project **edit**: `PATCH /api/v1/admin/projects/{id}/cover-image/{mediaFileId}` called after the main update
