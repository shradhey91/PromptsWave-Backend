# PromptsWave API Reference

## Base URL
```
http://localhost:8085
```

## Authentication
Most endpoints require a **JWT Bearer token** in the `Authorization` header:
```
Authorization: Bearer <accessToken>
```

Tokens are obtained from the **Login** endpoint.

- **Access token** expires in **7 days**
- **Refresh token** expires in **30 days**
- Tokens are **HS512 signed JWTs**

---

## Response Format

All error responses follow this shape:
```json
{ "message": "Description of the error" }
```

Validation errors include a field-level breakdown:
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "Password must be at least 8 characters"
  }
}
```

Paginated responses:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 400 | Bad request / validation error |
| 401 | Unauthorized (wrong credentials / missing token) |
| 403 | Forbidden (valid token but insufficient role) |
| 404 | Resource not found |
| 409 | Conflict (e.g. email already registered) |
| 500 | Internal server error |

---

# AUTH ENDPOINTS
> Base path: `/api/auth` — No authentication required unless noted

---

## POST `/api/auth/register`
Register a new user account. Sends a verification email.

**Request Body**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Secret123",
  "confirmPassword": "Secret123",
  "profileIconUrl": "https://example.com/avatar.png",
  "country": "India",
  "referralSource": "Twitter"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | Yes | Non-empty |
| email | Yes | Valid email format |
| password | Yes | Min 8 characters |
| confirmPassword | Yes | Must match password |
| profileIconUrl | No | URL string |
| country | No | |
| referralSource | No | |

**Success Response — 200**
```json
{ "message": "Registration successful. Please check your email to verify your account." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Validation failed (with errors object) |
| 400 | Passwords do not match |
| 409 | Email is already registered |

---

## POST `/api/auth/login`
Login and receive JWT tokens.

**Request Body**
```json
{
  "email": "john@example.com",
  "password": "Secret123"
}
```

**Success Response — 200**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "profileIconUrl": "https://example.com/avatar.png",
    "country": "India",
    "referralSource": "Twitter",
    "role": "USER",
    "isEmailVerified": true,
    "createdAt": "2026-06-07T10:00:00",
    "lastLoginAt": "2026-06-07T12:00:00"
  }
}
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Please verify your email before logging in. |
| 401 | Invalid email or password |
| 403 | Your account has been suspended. Please contact support. |

---

## POST `/api/auth/logout`
Invalidates the current access token (blacklists it server-side).

**Headers**
```
Authorization: Bearer <accessToken>
```

**Success Response — 200**
```json
{ "message": "Logged out successfully" }
```

> The token is blacklisted immediately. Any subsequent request with the same token will be rejected.

---

## POST `/api/auth/refresh`
Get a new access token using a refresh token.

**Headers**
```
Authorization: Bearer <refreshToken>
```

**Success Response — 200**
```json
{ "accessToken": "eyJhbGciOiJIUzUxMiJ9..." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Missing refresh token |
| 400 | Provided token is not a refresh token |
| 400 | Refresh token is invalid or expired |

---

## GET `/api/auth/verify?token={token}`
Verify email address using the token sent to the user's email.

**Query Params**
| Param | Required | Notes |
|-------|----------|-------|
| token | Yes | UUID token from verification email |

**Success Response — 200**
```json
{ "message": "Email verified successfully. You can now log in." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | This verification link has already been used |
| 400 | Verification link has expired. Please request a new one. |
| 404 | Invalid verification token |

---

## POST `/api/auth/resend-verification`
Resend the email verification link.

**Request Body**
```json
{ "email": "john@example.com" }
```

**Success Response — 200**
```json
{ "message": "Verification email resent. Please check your inbox." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Email is already verified |
| 404 | No account found with that email |

---

## POST `/api/auth/forgot-password`
Request a password reset link via email. Link expires in 1 hour.

**Request Body**
```json
{ "email": "john@example.com" }
```

**Success Response — 200**
```json
{ "message": "Password reset link sent to your email." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Validation failed |
| 404 | No account found with that email |

---

## POST `/api/auth/reset-password`
Reset password using the token from the reset email.

**Request Body**
```json
{
  "token": "uuid-token-from-email",
  "newPassword": "NewSecret123"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| token | Yes | UUID from reset email |
| newPassword | Yes | Min 8 characters |

**Success Response — 200**
```json
{ "message": "Password reset successfully. You can now log in." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | This reset link has already been used |
| 400 | Reset link has expired. Please request a new one. |
| 404 | Invalid or expired reset token |

---

## POST `/api/auth/change-password`
Change password for the logged-in user.

**Auth Required: Yes**

**Request Body**
```json
{
  "currentPassword": "OldSecret123",
  "newPassword": "NewSecret456"
}
```

**Success Response — 200**
```json
{ "message": "Password changed successfully." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Validation failed |
| 403 | Current password is incorrect |

---

## POST `/api/auth/change-email`
Change email address. Sends a new verification email to the new address.

**Auth Required: Yes**

**Request Body**
```json
{
  "newEmail": "newemail@example.com",
  "password": "CurrentSecret123"
}
```

**Success Response — 200**
```json
{ "message": "Email updated. Please verify your new email address." }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 403 | Password is incorrect |
| 409 | This email is already in use |

---

# USER PROFILE ENDPOINTS
> Base path: `/api/users/me` — Authentication required

---

## GET `/api/users/me`
Get the current user's profile.

**Success Response — 200**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "profileIconUrl": "https://example.com/avatar.png",
  "country": "India",
  "referralSource": "Twitter",
  "role": "USER",
  "isEmailVerified": true,
  "createdAt": "2026-06-07T10:00:00",
  "lastLoginAt": "2026-06-07T12:00:00"
}
```

---

## PUT `/api/users/me`
Update the current user's name, profile icon, and country.

**Request Body**
```json
{
  "name": "John Updated",
  "profileIconUrl": "https://example.com/new-avatar.png",
  "country": "US"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | Yes | Non-empty |
| profileIconUrl | No | Pass null to clear |
| country | No | |

**Success Response — 200** — returns updated `UserProfileResponse` (same shape as GET)

---

## DELETE `/api/users/me`
Deactivate and soft-delete the current user's account. The email is scrambled so the address can be re-registered.

**Success Response — 200**
```json
{ "message": "Account deleted successfully" }
```

---

# USER INTERACTIONS ENDPOINTS
> Base path: `/api/user/interactions` — Authentication required

---

## GET `/api/user/interactions/status/{promptId}`
Get the current like/save status and counters for a specific prompt.

**Success Response — 200**
```json
{
  "promptId": 5,
  "liked": false,
  "saved": true,
  "totalLikes": 42,
  "totalCopies": 130
}
```

**Error Responses**
| Status | Message |
|--------|---------|
| 404 | Prompt not found |

---

## POST `/api/user/interactions/like/{promptId}`
Toggle like on a prompt (like if not liked, unlike if already liked).

**Success Response — 200** — returns `InteractionStatusResponse` (same shape as status endpoint)

---

## POST `/api/user/interactions/save/{promptId}`
Toggle save on a prompt (save if not saved, unsave if already saved).

**Success Response — 200** — returns `InteractionStatusResponse`

---

## POST `/api/user/interactions/copy/{promptId}`
Record a copy event for an authenticated user.

**Success Response — 200**
```json
{ "message": "Copied successfully" }
```

---

## GET `/api/user/interactions/liked`
Get all prompts liked by the current user (paginated).

**Query Params**
| Param | Default | Notes |
|-------|---------|-------|
| page | 0 | Zero-based page index |
| size | 20 | Items per page |

**Success Response — 200** — `PagedResponse<PromptSummaryResponse>`
```json
{
  "content": [
    {
      "id": 5,
      "title": "Blog Post Writer",
      "description": "Writes SEO-friendly blog posts",
      "imageUrl": "https://example.com/img.png",
      "categoryName": "Writing",
      "categorySlug": "writing",
      "recommendedAiNames": ["ChatGPT", "Claude"],
      "timesCopied": 130,
      "likesCount": 42,
      "createdAt": "2026-06-01T08:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1,
  "last": true
}
```

---

## GET `/api/user/interactions/saved`
Get all prompts saved by the current user (paginated).

**Query Params:** same as `/liked`

**Success Response — 200** — `PagedResponse<PromptSummaryResponse>` (same shape as liked)

---

## GET `/api/user/interactions/history`
Get the current user's prompt copy history (paginated).

**Query Params:** same as `/liked`

**Success Response — 200**
```json
{
  "content": [
    {
      "promptId": 5,
      "promptTitle": "Blog Post Writer",
      "promptDescription": "Writes SEO-friendly blog posts",
      "imageUrl": "https://example.com/img.png",
      "categoryName": "Writing",
      "categorySlug": "writing",
      "recommendedAiNames": ["ChatGPT"],
      "copiedAt": "2026-06-07T11:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

# PUBLIC ENDPOINTS
> Base path: `/api/public` — No authentication required

---

## GET `/api/public/prompts`
Browse all published prompts with optional filters and sorting.

**Query Params**
| Param | Default | Options / Notes |
|-------|---------|-----------------|
| sort | newest | `newest` \| `mostLiked` \| `mostUsed` \| `trending` |
| categoryId | — | Filter by category ID |
| aiEntityId | — | Filter by AI entity ID |
| search | — | Full-text search on title and description |
| page | 0 | Zero-based page index |
| size | 20 | Items per page |

> Note: `search` and `categoryId` can be combined. `aiEntityId` is applied only when no `search` or `categoryId` is present.

**Success Response — 200** — `PagedResponse<PromptSummaryResponse>` (same shape as user liked prompts)

---

## GET `/api/public/prompts/{id}`
Get full details of a single published prompt.

**Success Response — 200**
```json
{
  "id": 5,
  "title": "Blog Post Writer",
  "promptText": "Write a detailed blog post about [TOPIC]...",
  "description": "Writes SEO-friendly blog posts",
  "imageUrl": "https://example.com/img.png",
  "category": {
    "id": 2,
    "name": "Writing",
    "slug": "writing",
    "description": "Writing prompts",
    "iconUrl": "https://example.com/icon.png",
    "level": 0,
    "sortOrder": 1,
    "isActive": true,
    "parentId": null,
    "parentName": null,
    "promptCount": 0
  },
  "recommendedAiEntities": [
    {
      "id": 1,
      "name": "ChatGPT",
      "slug": "chatgpt",
      "iconUrl": "https://example.com/gpt.png",
      "rank": 1
    }
  ],
  "timesCopied": 130,
  "likesCount": 42,
  "isPublished": true,
  "uploadedBy": "Platform Admin",
  "createdAt": "2026-06-01T08:00:00",
  "updatedAt": "2026-06-05T09:00:00"
}
```

**Error Responses**
| Status | Message |
|--------|---------|
| 404 | Prompt not found |

---

## GET `/api/public/categories`
Get the full category tree (top-level categories with their subcategories).

**Success Response — 200**
```json
[
  {
    "id": 1,
    "name": "Writing",
    "slug": "writing",
    "description": "All writing prompts",
    "iconUrl": "https://example.com/icon.png",
    "sortOrder": 1,
    "promptCount": 24,
    "subcategories": [
      {
        "id": 2,
        "name": "Blog Posts",
        "slug": "blog-posts",
        "description": "Blog writing prompts",
        "iconUrl": null,
        "level": 1,
        "sortOrder": 1,
        "isActive": true,
        "parentId": 1,
        "parentName": "Writing",
        "promptCount": 10
      }
    ]
  }
]
```

---

## GET `/api/public/categories/{slug}`
Get a single category by its slug.

**Success Response — 200**
```json
{
  "id": 1,
  "name": "Writing",
  "slug": "writing",
  "description": "All writing prompts",
  "iconUrl": "https://example.com/icon.png",
  "level": 0,
  "sortOrder": 1,
  "isActive": true,
  "parentId": null,
  "parentName": null,
  "promptCount": 24
}
```

**Error Responses**
| Status | Message |
|--------|---------|
| 404 | Category not found |

---

## GET `/api/public/ai-entities`
Get all active AI entities (used for filter dropdowns).

**Success Response — 200**
```json
[
  {
    "id": 1,
    "name": "ChatGPT",
    "slug": "chatgpt",
    "iconUrl": "https://example.com/gpt.png",
    "description": "OpenAI GPT-4o",
    "websiteUrl": "https://chat.openai.com",
    "isActive": true
  }
]
```

---

## GET `/api/public/ai-entities/{id}`
Get a single AI entity by ID.

**Success Response — 200** — `AiEntityResponse` (same shape as above)

**Error Responses**
| Status | Message |
|--------|---------|
| 404 | AI entity not found |

---

## POST `/api/public/interactions/copy/{promptId}`
Record a copy event for a guest user (no account required).

**Success Response — 200**
```json
{ "message": "Copied successfully" }
```

---

# ADMIN ENDPOINTS
> Base path: `/api/admin` — ADMIN role required

---

## GET `/api/admin/dashboard/stats`
Get platform-wide statistics.

**Success Response — 200**
```json
{
  "totalUsers": 1500,
  "totalPrompts": 200,
  "publishedPrompts": 180,
  "draftPrompts": 20,
  "totalCategories": 15,
  "totalAiEntities": 8,
  "totalLikes": 4200,
  "totalCopies": 13000
}
```

---

## GET `/api/admin/dashboard/top-liked`
Get top N most liked published prompts.

**Query Params**
| Param | Default |
|-------|---------|
| limit | 10 |

**Success Response — 200**
```json
[
  {
    "id": 5,
    "title": "Blog Post Writer",
    "categoryName": "Writing",
    "imageUrl": "https://example.com/img.png",
    "likesCount": 420,
    "timesCopied": 1300
  }
]
```

---

## GET `/api/admin/dashboard/top-copied`
Get top N most copied published prompts.

**Query Params:** same as top-liked

**Success Response — 200** — same shape as top-liked

---

## GET `/api/admin/users`
List all users (paginated, with optional search).

**Query Params**
| Param | Default | Notes |
|-------|---------|-------|
| search | — | Searches name and email |
| page | 0 | |
| size | 20 | |

**Success Response — 200** — `PagedResponse<AdminUserResponse>`
```json
{
  "content": [
    {
      "id": 3,
      "name": "Jane Smith",
      "email": "jane@example.com",
      "country": "US",
      "referralSource": "Google",
      "role": "USER",
      "isEmailVerified": true,
      "isActive": true,
      "createdAt": "2026-05-01T10:00:00",
      "lastLoginAt": "2026-06-06T09:00:00",
      "likedPromptsCount": 12,
      "savedPromptsCount": 5,
      "copyEventsCount": 30
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```

---

## GET `/api/admin/users/{userId}`
Get full details and activity stats for a single user.

**Success Response — 200** — `AdminUserResponse` (same shape as above)

---

## PATCH `/api/admin/users/{userId}/suspend`
Suspend a user account (blocks login).

**Success Response — 200** — `AdminUserResponse` with `isActive: false`

**Error Responses**
| Status | Message |
|--------|---------|
| 409 | User is already suspended |
| 403 | Admin accounts cannot be managed through this endpoint |

---

## PATCH `/api/admin/users/{userId}/reactivate`
Reactivate a suspended user account.

**Success Response — 200** — `AdminUserResponse` with `isActive: true`

**Error Responses**
| Status | Message |
|--------|---------|
| 409 | User is already active |

---

## DELETE `/api/admin/users/{userId}`
Permanently delete a user account and all associated data.

**Success Response — 200**
```json
{ "message": "User deleted successfully" }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 403 | Admin accounts cannot be managed through this endpoint |
| 404 | User not found |

---

## GET `/api/admin/prompts`
List all prompts (published + drafts) with optional search/filter.

**Query Params**
| Param | Default | Notes |
|-------|---------|-------|
| search | — | Searches title and description |
| categoryId | — | Filter by category |
| page | 0 | |
| size | 20 | |

**Success Response — 200** — `PagedResponse<PromptResponse>` (full prompt detail shape)

---

## GET `/api/admin/prompts/{id}`
Get a single prompt by ID (includes unpublished).

**Success Response — 200** — `PromptResponse` (same full shape as public prompt detail)

---

## POST `/api/admin/prompts`
Create a new prompt.

**Request Body**
```json
{
  "title": "Blog Post Writer",
  "promptText": "Write a detailed blog post about [TOPIC] in a professional tone.",
  "description": "Versatile blog writing prompt",
  "categoryId": 2,
  "imageUrl": "https://example.com/img.png",
  "recommendedAiEntityIds": [1, 3],
  "publish": true
}
```

| Field | Required | Notes |
|-------|----------|-------|
| title | Yes | |
| promptText | Yes | The actual prompt content |
| description | No | Short summary |
| categoryId | Yes | Must be an existing category ID |
| imageUrl | No | |
| recommendedAiEntityIds | Yes | At least one AI entity ID; order determines rank |
| publish | No | `false` by default (saves as draft) |

**Success Response — 201** — `PromptResponse`

---

## PUT `/api/admin/prompts/{id}`
Update an existing prompt.

**Request Body**
```json
{
  "title": "Updated Title",
  "promptText": "Updated prompt text...",
  "description": "Updated description",
  "categoryId": 2,
  "imageUrl": "https://example.com/new-img.png",
  "recommendedAiEntityIds": [1],
  "isPublished": true
}
```

| Field | Required | Notes |
|-------|----------|-------|
| title | Yes | |
| promptText | Yes | |
| categoryId | Yes | |
| recommendedAiEntityIds | Yes | Replaces existing AI entity links |
| description | No | |
| imageUrl | No | |
| isPublished | No | |

**Success Response — 200** — `PromptResponse`

---

## PATCH `/api/admin/prompts/{id}/toggle-publish`
Toggle publish/unpublish state of a prompt.

**Success Response — 200** — `PromptResponse` with updated `isPublished`

---

## DELETE `/api/admin/prompts/{id}`
Permanently delete a prompt and all its associated likes, saves, and copy records.

**Success Response — 200**
```json
{ "message": "Prompt deleted successfully" }
```

---

## GET `/api/admin/categories`
List all categories including inactive ones.

**Success Response — 200** — `List<CategoryResponse>`
```json
[
  {
    "id": 1,
    "name": "Writing",
    "slug": "writing",
    "description": "All writing prompts",
    "iconUrl": "https://example.com/icon.png",
    "level": 0,
    "sortOrder": 1,
    "isActive": true,
    "parentId": null,
    "parentName": null,
    "promptCount": 24
  }
]
```

---

## POST `/api/admin/categories`
Create a new category or subcategory.

**Request Body**
```json
{
  "name": "Blog Posts",
  "slug": "blog-posts",
  "description": "Blog writing prompts",
  "iconUrl": "https://example.com/icon.png",
  "parentId": 1,
  "sortOrder": 1
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | Yes | |
| slug | Yes | Unique URL-friendly identifier |
| description | No | |
| iconUrl | No | |
| parentId | No | If set, creates a subcategory under that parent |
| sortOrder | No | |

**Success Response — 201** — `CategoryResponse`

**Error Responses**
| Status | Message |
|--------|---------|
| 409 | Slug already exists |

---

## PUT `/api/admin/categories/{id}`
Update category details.

**Request Body**
```json
{
  "name": "Updated Name",
  "description": "Updated description",
  "iconUrl": "https://example.com/new-icon.png",
  "sortOrder": 2,
  "isActive": true
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | Yes | |
| description | No | |
| iconUrl | No | |
| sortOrder | No | |
| isActive | No | Set to `false` to hide from public |

**Success Response — 200** — `CategoryResponse`

---

## DELETE `/api/admin/categories/{id}`
Soft-delete a category. Only succeeds if no active prompts belong to it.

**Success Response — 200**
```json
{ "message": "Category deleted successfully" }
```

**Error Responses**
| Status | Message |
|--------|---------|
| 400 | Cannot delete category with N active prompts. Move or delete those prompts first. |

---

## GET `/api/admin/ai-entities`
List all AI entities including inactive ones.

**Success Response — 200** — `List<AiEntityResponse>`

---

## POST `/api/admin/ai-entities`
Add a new AI entity.

**Request Body**
```json
{
  "name": "Claude",
  "slug": "claude",
  "iconUrl": "https://example.com/claude.png",
  "description": "Anthropic Claude",
  "websiteUrl": "https://claude.ai"
}
```

| Field | Required | Notes |
|-------|----------|-------|
| name | Yes | |
| slug | Yes | Unique |
| iconUrl | No | |
| description | No | |
| websiteUrl | No | |

**Success Response — 201** — `AiEntityResponse`

---

## PUT `/api/admin/ai-entities/{id}`
Update an AI entity.

**Request Body** — same shape as POST

**Success Response — 200** — `AiEntityResponse`

---

## PATCH `/api/admin/ai-entities/{id}/toggle-active`
Toggle active/inactive status of an AI entity. Inactive entities are hidden from public filters.

**Success Response — 200**
```json
{ "message": "AI entity status toggled" }
```

---

# QUICK REFERENCE

## Roles
| Role | Access |
|------|--------|
| Guest (no token) | Public endpoints only |
| USER | Public + user profile + user interactions |
| ADMIN | Everything including all `/api/admin/**` routes |

## Common Flows

**New user flow:**
1. `POST /api/auth/register`
2. User clicks link in email → `GET /api/auth/verify?token=...`
3. `POST /api/auth/login` → store `accessToken` and `refreshToken`

**Token refresh flow:**
1. Call `POST /api/auth/refresh` with `refreshToken` in `Authorization` header
2. Store the new `accessToken`

**Copy a prompt (guest):**
1. `POST /api/public/interactions/copy/{promptId}`

**Copy a prompt (logged-in user):**
1. `POST /api/user/interactions/copy/{promptId}` with `accessToken`
