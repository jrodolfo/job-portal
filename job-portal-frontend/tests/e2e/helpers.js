import { expect } from '@playwright/test';

export async function loginAsAdmin(page) {
    await login(page, 'admin@local.test', 'admin123', '/admin-dashboard');
}

export async function loginAsApplicant(page) {
    await login(page, 'user@local.test', 'user123', '/applicant-dashboard');
}

export async function login(page, email, password, expectedPath) {
    await page.goto(expectedPath);
    await expect(page).toHaveURL(/\/$/);

    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Password').fill(password);
    await page.getByRole('button', { name: 'Login' }).click();

    await expect(page).toHaveURL(new RegExp(`${expectedPath.replace('/', '\\/')}$`));
}

export async function openAdminTab(page, name) {
    await page.getByRole('tab', { name }).click();
}

export async function createJob(page, { title, company, description }) {
    await openAdminTab(page, 'Add Job');
    await page.getByLabel('Title').fill(title);
    await page.getByLabel('Company').fill(company);
    await page.getByLabel('Description').fill(description);
    await page.getByRole('button', { name: 'Create Job' }).click();
}

export function getJobCard(page, title) {
    return page.locator('.job-card').filter({ hasText: title }).first();
}
