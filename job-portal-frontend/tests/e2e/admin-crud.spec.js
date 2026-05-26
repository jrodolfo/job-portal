import { expect, test } from '@playwright/test';
import { createJob, getJobCard, loginAsAdmin, loginAsApplicant, openAdminTab } from './helpers';

test('admin can create, edit, and delete a job from the dashboard', async ({ page }) => {
    const uniqueId = Date.now();
    const createdTitle = `e2e admin job ${uniqueId}`;
    const updatedTitle = `${createdTitle} updated`;

    await loginAsAdmin(page);
    await expect(page.getByRole('heading', { level: 1, name: 'Admin', exact: true })).toBeVisible();

    await createJob(page, {
        title: createdTitle,
        company: 'ACME E2E',
        description: 'Created from the Playwright admin CRUD smoke test.'
    });

    await expect(page.getByText('Job created successfully.')).toBeVisible();
    const createdCard = getJobCard(page, createdTitle);
    await expect(createdCard).toBeVisible();

    await createdCard.getByRole('button', { name: 'Edit' }).click();
    await expect(page.getByRole('heading', { name: 'Edit Job' })).toBeVisible();

    await page.getByLabel('Title').fill(updatedTitle);
    await page.getByLabel('Description').fill('Updated from the Playwright admin CRUD smoke test.');
    await page.getByRole('button', { name: 'Save Changes' }).click();

    await expect(page.getByText('Job updated successfully.')).toBeVisible();
    const updatedCard = getJobCard(page, updatedTitle);
    await expect(updatedCard).toBeVisible();

    page.once('dialog', async (dialog) => {
        expect(dialog.message()).toContain('Delete this job?');
        await dialog.accept();
    });
    await updatedCard.getByRole('button', { name: 'Delete' }).click();

    await expect(page.getByText('Job deleted successfully.')).toBeVisible();
    await expect(page.locator('.job-card').filter({ hasText: updatedTitle })).toHaveCount(0);
});

test('admin returns to jobs after canceling edit mode', async ({ page }) => {
    const uniqueId = Date.now();
    const title = `e2e cancel edit ${uniqueId}`;

    await loginAsAdmin(page);
    await expect(page.getByRole('heading', { level: 1, name: 'Admin', exact: true })).toBeVisible();

    await createJob(page, {
        title,
        company: 'ACME Cancel',
        description: 'Created to verify cancel edit navigation.'
    });

    await expect(page.getByText('Job created successfully.')).toBeVisible();
    const jobCard = getJobCard(page, title);
    await expect(jobCard).toBeVisible();

    await jobCard.getByRole('button', { name: 'Edit' }).click();
    await expect(page.getByRole('tab', { name: 'Add Job' })).toHaveAttribute('aria-selected', 'true');
    await expect(page.getByRole('button', { name: 'Cancel Edit' })).toBeVisible();

    await page.getByRole('button', { name: 'Cancel Edit' }).click();

    await expect(page.getByRole('tab', { name: 'Jobs' })).toHaveAttribute('aria-selected', 'true');
    await expect(page.getByRole('button', { name: 'Cancel Edit' })).toHaveCount(0);
    await expect(getJobCard(page, title)).toBeVisible();

    page.once('dialog', async (dialog) => {
        expect(dialog.message()).toContain('Delete this job?');
        await dialog.accept();
    });
    await getJobCard(page, title).getByRole('button', { name: 'Delete' }).click();

    await expect(page.getByText('Job deleted successfully.')).toBeVisible();
    await expect(page.locator('.job-card').filter({ hasText: title })).toHaveCount(0);
});

test('admin can review an applicant application from the dashboard', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e admin review ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await loginAsAdmin(adminPage);

    await createJob(adminPage, {
        title,
        company: 'ACME Review',
        description: 'Created to verify admin application review.'
    });

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();

    await loginAsApplicant(applicantPage);
    const applicantCard = getJobCard(applicantPage, title);
    await expect(applicantCard).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply' }).click();
    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();

    await adminPage.reload();
    const adminCard = getJobCard(adminPage, title);
    await expect(adminCard.getByText('Applications: 1')).toBeVisible();

    await openAdminTab(adminPage, 'Applications');
    const applicationSection = adminPage.locator('.card').filter({ hasText: `Applicant: user` }).filter({ hasText: title }).first();
    await expect(applicationSection).toBeVisible();
    await applicationSection.getByLabel('Status').selectOption('REVIEWING');
    await applicationSection.getByRole('button', { name: 'Save' }).click();

    await expect(adminPage.getByText('Application status updated successfully.')).toBeVisible();
    await expect(applicationSection.getByText('Current Status: Reviewing')).toBeVisible();

    await applicantPage.reload();
    const refreshedApplicantCard = getJobCard(applicantPage, title);
    await expect(refreshedApplicantCard.getByText('Application Status: Reviewing')).toBeVisible();
    await expect(refreshedApplicantCard.getByText('Your application is currently under review.')).toBeVisible();

    await adminContext.close();
    await applicantContext.close();
});

test('admin overview and application timestamps are visible during review flow', async ({ browser }) => {
    const uniqueId = Date.now();
    const title = `e2e overview timestamps ${uniqueId}`;

    const adminContext = await browser.newContext();
    const applicantContext = await browser.newContext();
    const adminPage = await adminContext.newPage();
    const applicantPage = await applicantContext.newPage();

    await loginAsAdmin(adminPage);

    await expect(adminPage.getByText('Total Jobs')).toBeVisible();
    await expect(adminPage.getByText('Open Jobs')).toBeVisible();
    await expect(adminPage.getByText('Closed Jobs')).toBeVisible();
    await expect(adminPage.locator('.admin-overview-card').filter({ hasText: 'Applications' }).first()).toBeVisible();

    await createJob(adminPage, {
        title,
        company: 'ACME Overview',
        description: 'Created to verify overview cards and application timestamps.'
    });

    await expect(adminPage.getByText('Job created successfully.')).toBeVisible();

    await loginAsApplicant(applicantPage);
    const applicantCard = getJobCard(applicantPage, title);
    await expect(applicantCard).toBeVisible();
    await applicantCard.getByRole('button', { name: 'Apply' }).click();
    await expect(applicantPage.getByText('Application submitted successfully.')).toBeVisible();
    await expect(applicantCard.getByText(/Applied On:/)).toBeVisible();
    await expect(applicantCard.getByText('Your application has been submitted and is waiting for review.')).toBeVisible();

    await adminPage.reload();
    await openAdminTab(adminPage, 'Applications');
    const applicationSection = adminPage.locator('.application-card').filter({ hasText: 'Applicant: user' }).filter({ hasText: title }).first();
    await expect(applicationSection).toBeVisible();
    await expect(applicationSection.getByText(/Applied On:/)).toBeVisible();

    await applicationSection.getByLabel('Status').selectOption('REVIEWING');
    await applicationSection.getByRole('button', { name: 'Save' }).click();

    await expect(adminPage.getByText('Application status updated successfully.')).toBeVisible();
    await expect(applicationSection.getByText('Current Status: Reviewing')).toBeVisible();
    await expect(applicationSection.getByText(/Last Updated:/)).toBeVisible();

    await applicantPage.reload();
    const refreshedApplicantCard = getJobCard(applicantPage, title);
    await expect(refreshedApplicantCard.getByText('Application Status: Reviewing')).toBeVisible();
    await expect(refreshedApplicantCard.getByText(/Applied On:/)).toBeVisible();
    await expect(refreshedApplicantCard.getByText(/Last Updated:/)).toBeVisible();

    await adminContext.close();
    await applicantContext.close();
});
