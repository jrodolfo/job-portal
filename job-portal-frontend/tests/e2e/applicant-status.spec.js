import { expect, test } from '@playwright/test';
import { createJob, getJobCard, loginAsAdmin, loginAsApplicant } from './helpers';

test('applicant can withdraw and reapply after submitting an application', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e applicant status ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await loginAsAdmin(adminPage);

    await createJob(adminPage, {
        title,
        company: 'ACME Status',
        description: 'Created to verify applicant application status behavior.'
    });

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();
    const adminCard = getJobCard(adminPage, title);
    await expect(adminCard).toBeVisible();

    await loginAsApplicant(applicantPage);

    const applicantCard = getJobCard(applicantPage, title);
    await expect(applicantCard).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Not applied')).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply' }).click();

    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Applied')).toBeVisible();
    await expect(applicantCard.getByRole('button', { name: 'Withdraw' })).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Withdraw' }).click();
    await expect(applicantPage.getByText('Application withdrawn successfully.')).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Withdrawn')).toBeVisible();
    await expect(applicantCard.getByRole('button', { name: 'Apply Again' })).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply Again' }).click();
    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();
    await expect(applicantCard.getByText('Application Status: Applied')).toBeVisible();
    await expect(applicantCard.getByRole('button', { name: 'Withdraw' })).toBeVisible();

    await applicantPage.reload();
    const refreshedApplicantCard = getJobCard(applicantPage, title);
    await expect(refreshedApplicantCard.getByText('Application Status: Applied')).toBeVisible();
    await expect(refreshedApplicantCard.getByRole('button', { name: 'Withdraw' })).toBeVisible();

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
