import { expect, test } from '@playwright/test';
import { createJob, getJobCard, loginAsAdmin, loginAsApplicant } from './helpers';

test('applicant sees jobs created by admin and no longer sees them after deletion', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e applicant visibility ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await loginAsAdmin(adminPage);

    await createJob(adminPage, {
        title,
        company: 'ACME Visibility',
        description: 'Created to verify applicant job-list visibility.'
    });

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();
    const adminCard = getJobCard(adminPage, title);
    await expect(adminCard).toBeVisible();

    await loginAsApplicant(applicantPage);

    const applicantCard = getJobCard(applicantPage, title);
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
