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

test('admin can review an applicant application from the dashboard', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e admin review ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await pageLogin(adminPage, 'admin', 'admin123', '/admin-dashboard');

    await adminPage.getByLabel('Title').fill(title);
    await adminPage.getByLabel('Company').fill('ACME Review');
    await adminPage.getByLabel('Description').fill('Created to verify admin application review.');
    await adminPage.getByRole('button', { name: 'Create Job' }).click();

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();

    await pageLogin(applicantPage, 'user', 'user123', '/applicant-dashboard');
    const applicantCard = applicantPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(applicantCard).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply' }).click();
    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();

    await adminPage.reload();
    const adminCard = adminPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(adminCard.getByText('Applications: 1')).toBeVisible();

    const applicationSection = adminPage.locator('.card').filter({ hasText: `Applicant: user` }).filter({ hasText: title }).first();
    await expect(applicationSection).toBeVisible();
    await applicationSection.getByLabel('Update Status').selectOption('REVIEWING');
    await applicationSection.getByRole('button', { name: 'Save Status' }).click();

    await expect(adminPage.getByText('Application status updated successfully.')).toBeVisible();
    await expect(applicationSection.getByText('Current Status: Reviewing')).toBeVisible();

    await applicantPage.reload();
    const refreshedApplicantCard = applicantPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(refreshedApplicantCard.getByText('Application Status: Reviewing')).toBeVisible();
    await expect(refreshedApplicantCard.getByText('Your application is currently under review.')).toBeVisible();

    await adminContext.close();
    await applicantContext.close();
});

async function pageLogin(page, username, password, expectedPath) {
    await page.goto(expectedPath);
    await expect(page).toHaveURL(/\/$/);

    await page.getByPlaceholder('Username').fill(username);
    await page.getByPlaceholder('Password').fill(password);
    await page.getByRole('button', { name: 'Login' }).click();

    await expect(page).toHaveURL(new RegExp(`${expectedPath.replace('/', '\\/')}$`));
}
