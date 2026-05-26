import { expect, test } from '@playwright/test';

async function login(page, username, password, expectedPath) {
    await page.goto(expectedPath);
    await page.getByPlaceholder('Username').fill(username);
    await page.getByPlaceholder('Password').fill(password);
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).toHaveURL(new RegExp(`${expectedPath.replace('/', '\\/')}$`));
}

test('applicant sees jobs created by admin and no longer sees them after deletion', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e applicant visibility ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await login(adminPage, 'admin', 'admin123', '/admin-dashboard');

    await adminPage.getByLabel('Title').fill(title);
    await adminPage.getByLabel('Company').fill('ACME Visibility');
    await adminPage.getByLabel('Description').fill('Created to verify applicant job-list visibility.');
    await adminPage.getByRole('button', { name: 'Create Job' }).click();

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();
    const adminCard = adminPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(adminCard).toBeVisible();

    await login(applicantPage, 'user', 'user123', '/applicant-dashboard');

    const applicantCard = applicantPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(applicantCard).toBeVisible();
    await expect(applicantCard.getByText('Company: ACME Visibility')).toBeVisible();

    adminPage.once('dialog', async (dialog) => {
        expect(dialog.message()).toContain('Delete this job?');
        await dialog.accept();
    });
    await adminCard.getByRole('button', { name: 'Delete' }).click();

    await expect(adminPage.getByText('Job deleted successfully.')).toBeVisible();
    await expect(adminPage.locator('.job-card').filter({ hasText: title })).toHaveCount(0);

    await applicantPage.reload();
    await expect(applicantPage.locator('.job-card').filter({ hasText: title })).toHaveCount(0);

    await adminContext.close();
    await applicantContext.close();
});
