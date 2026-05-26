import { expect, test } from '@playwright/test';

test('admin can create, edit, and delete a job from the dashboard', async ({ page }) => {
    const uniqueId = Date.now();
    const createdTitle = `e2e admin job ${uniqueId}`;
    const updatedTitle = `${createdTitle} updated`;

    await page.goto('/admin-dashboard');
    await expect(page).toHaveURL(/\/$/);

    await page.getByPlaceholder('Username').fill('admin');
    await page.getByPlaceholder('Password').fill('admin123');
    await page.getByRole('button', { name: 'Login' }).click();

    await expect(page).toHaveURL(/\/admin-dashboard$/);
    await expect(page.getByRole('heading', { name: 'Admin Dashboard' })).toBeVisible();

    await page.getByLabel('Title').fill(createdTitle);
    await page.getByLabel('Company').fill('ACME E2E');
    await page.getByLabel('Description').fill('Created from the Playwright admin CRUD smoke test.');
    await page.getByRole('button', { name: 'Create Job' }).click();

    await expect(page.getByText('Job created successfully.')).toBeVisible();
    const createdCard = page.locator('.job-card').filter({ hasText: createdTitle }).first();
    await expect(createdCard).toBeVisible();

    await createdCard.getByRole('button', { name: 'Edit' }).click();
    await expect(page.getByRole('heading', { name: 'Edit Job' })).toBeVisible();

    await page.getByLabel('Title').fill(updatedTitle);
    await page.getByLabel('Description').fill('Updated from the Playwright admin CRUD smoke test.');
    await page.getByRole('button', { name: 'Save Changes' }).click();

    await expect(page.getByText('Job updated successfully.')).toBeVisible();
    const updatedCard = page.locator('.job-card').filter({ hasText: updatedTitle }).first();
    await expect(updatedCard).toBeVisible();

    page.once('dialog', async (dialog) => {
        expect(dialog.message()).toContain('Delete this job?');
        await dialog.accept();
    });
    await updatedCard.getByRole('button', { name: 'Delete' }).click();

    await expect(page.getByText('Job deleted successfully.')).toBeVisible();
    await expect(page.locator('.job-card').filter({ hasText: updatedTitle })).toHaveCount(0);
});
