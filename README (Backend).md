# Finance Backend

Backend REST API untuk aplikasi manajemen keuangan pribadi.

## Tech Stack
- Node.js
- Express 5
- Prisma ORM + PostgreSQL (Neon)
- JWT Authentication
- bcrypt
- node-cron

## Fitur
- Register, login, dan endpoint profil user (`/auth/me`)
- CRUD transaksi
- Ringkasan bulanan transaksi (`/transactions/summary?month=YYYY-MM`)
- CRUD target tabungan
- Update progress target tabungan
- CRUD reminder
- Endpoint reminder jatuh tempo terdekat (`/reminders/upcoming`)
- Auto-savings bulanan via cron job

## Struktur Proyek
```text
finance-backend/
|-- lib/
|   `-- prisma.js
|-- prisma/
|   |-- schema.prisma
|   `-- migrations/
|-- src/
|   |-- controllers/
|   |-- jobs/
|   |   `-- autoSavings.job.js
|   |-- middlewares/
|   |-- routes/
|   |-- services/
|   `-- index.js
|-- .env
`-- package.json
```

## Environment Variables
Buat file `.env`:

```env
DATABASE_URL="postgresql://<user>:<password>@<host>/<db>?sslmode=require"
JWT_SECRET="your_jwt_secret"
```

## Instalasi dan Menjalankan Project
1. Install dependency:
```bash
npm install
```
2. Jalankan migrasi database (pilih salah satu sesuai kebutuhan):
```bash
npx prisma migrate deploy
```
atau saat development:
```bash
npx prisma migrate dev
```
3. Jalankan server:
```bash
npm run dev
```

Server berjalan di `http://localhost:3000`.

## Authentication
Gunakan header berikut untuk endpoint protected:

```http
Authorization: Bearer <token>
```

## API Endpoints

### Auth
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me` (protected)

Contoh request register/login:
```json
{
  "email": "user@mail.com",
  "password": "123456"
}
```

### Transactions (protected)
- `POST /transactions`
- `GET /transactions`
- `PUT /transactions/:id`
- `DELETE /transactions/:id`
- `GET /transactions/summary?month=2026-02`

Contoh body create transaction:
```json
{
  "title": "Gaji",
  "amount": 5000000,
  "type": "income",
  "category": "Salary",
  "date": "2026-02-23",
  "note": "Gaji Februari"
}
```

### Savings Goals (protected)
- `POST /savings`
- `GET /savings`
- `PUT /savings/:id/progress`
- `DELETE /savings/:id`

Contoh body create goal:
```json
{
  "title": "Dana Darurat",
  "targetAmount": 10000000,
  "deadline": "2026-12-31",
  "autoSaveDay": 25,
  "monthlyAmount": 500000
}
```

Contoh body update progress:
```json
{
  "amount": 250000
}
```

### Reminders (protected)
- `POST /reminders`
- `GET /reminders`
- `GET /reminders/upcoming`
- `PUT /reminders/:id/paid`
- `DELETE /reminders/:id`

Contoh body create reminder:
```json
{
  "title": "Bayar listrik",
  "amount": 350000,
  "type": "tagihan",
  "dueDate": "2026-03-01",
  "repeatInterval": "monthly"
}
```

## Auto Savings Job
File: `src/jobs/autoSavings.job.js`

Cron berjalan setiap hari pukul 00:00 server time (`0 0 * * *`):
- mencari `savingsGoal` dengan `autoSaveDay` sesuai tanggal hari ini
- menambahkan `monthlyAmount` ke `currentAmount`
- tidak melebihi `targetAmount`

## Deploy ke Railway
1. Push project ke GitHub.
2. Login ke Railway, lalu klik **New Project** -> **Deploy from GitHub repo**.
3. Pilih repository `finance-backend`.
4. Di service Railway, set environment variables:
```env
DATABASE_URL=postgresql://...
JWT_SECRET=your_secret
```
5. Set **Start Command** ke:
```bash
npm start
```
6. Jalankan migrasi database di Railway shell atau job sekali jalan:
```bash
npm run prisma:migrate:deploy
```
7. Redeploy service, lalu cek endpoint health sederhana (mis. `GET /auth/me` dengan token valid).

Catatan Railway:
- Railway otomatis inject `PORT`, dan app ini sudah membaca `process.env.PORT`.
- `node-cron` akan berjalan selama service backend aktif.
- Prisma Client digenerate saat install dependency melalui script `postinstall`.
- Setelah deploy pertama, rotate semua secret jika sebelumnya pernah tersimpan di `.env` lokal yang terpublikasi.

Troubleshooting Prisma (`Cannot find module '.prisma/client/default'`):
1. Pastikan deploy memakai commit terbaru (yang sudah punya `postinstall`).
2. Trigger redeploy dengan **Clear build cache**.
3. Jika perlu, jalankan manual di Railway shell:
```bash
npm run prisma:generate
```

## Catatan
- Port server menggunakan `process.env.PORT` dengan fallback `3000`.
- Error response menggunakan format:
```json
{ "error": "message" }
```
