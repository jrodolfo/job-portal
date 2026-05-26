import { expect, test } from '@playwright/test';

async function login(page, username, password, expectedPath) {
    await page.goto(expectedPath);
    await page.getByPlaceholder('Username').fill(username);
    await page.getByPlaceholder('Password').fill(password);
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).toHaveURL(new RegExp(`${expectedPath.replace('/', '\\/')}$`));
}

test('applicant sees status and cannot reapply after submitting an application', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e applicant status ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await login(adminPage, 'admin', 'admin123', '/admin-dashboard');

    await adminPage.getByLabel('Title').fill(title);
    await adminPage.getByLabel('Company').fill('ACME Status');
    await adminPage.getByLabel('Description').fill('Created to verify applicant application status behavior.');
    await adminPage.getByRole('button', { name: 'Create Job' }).click();

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();
    const adminCard = adminPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(adminCard).toBeVisible();

    await login(applicantPage, 'user', 'user123', '/applicant-dashboard');

    const applicantCard = applicantPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(applicantCard).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Not applied')).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply' }).click();

    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Applied')).toBeVisible();
    await expect(applicantCard.getByRole('button', { name: 'Applied' })).toBeDisabled();
    await applicantPage.reload();
    const refreshedApplicantCard = applicantPage.locator('.job-card').filter({ hasText: title }).first();
    await expect(refreshedApplicantCard.getByText('Application Status: Applied')).toBeVisible();
    await expect(refreshedApplicantCard.getByRole('button', { name: 'Applied' })).toBeDisabled();

    adminPage.once('dialog', async (dialog) => {
        expect(dialog.message()).toContain('Delete this job?');
        await dialog.accept();
    });
    await adminCard.getByRole('button', { name: 'Delete' }).click();
    await expect(adminPage.getByText('Cannot delete job with existing applications')).toBeVisible();
    await expect(adminCard).toBeVisible();

    await adminContext.close();
    await applicantContext.close();
});
