import { expect, test } from '@playwright/test';

test('admin sees backend validation message when description exceeds the limit', async ({ page }) => {
    const uniqueId = Date.now();
    const title = `e2e invalid job ${uniqueId}`;
    const company = 'ACME Validation';
    const description = 'x'.repeat(2001);

    await page.goto('/admin-dashboard');
    await expect(page).toHaveURL(/\/$/);

    await page.getByPlaceholder('Username').fill('admin');
    await page.getByPlaceholder('Password').fill('admin123');
    await page.getByRole('button', { name: 'Login' }).click();

    await expect(page).toHaveURL(/\/admin-dashboard$/);
    await expect(page.getByRole('heading', { name: 'Admin Dashboard' })).toBeVisible();

    await page.getByLabel('Title').fill(title);
    await page.getByLabel('Company').fill(company);
    await page.getByLabel('Description').fill(description);
    await page.getByRole('button', { name: 'Create Job' }).click();

    await expect(page.getByText('Description must be at most 2000 characters')).toBeVisible();
    await expect(page.getByText('Job created successfully.')).toHaveCount(0);
    await expect(page.locator('.job-card').filter({ hasText: title })).toHaveCount(0);
});
